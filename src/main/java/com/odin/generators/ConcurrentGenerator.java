package com.odin.generators;

import com.odin.detection.Stack;
import com.odin.llm.LLMClient;
import com.odin.llm.LLMClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The type Concurrent generator.
 */
public class ConcurrentGenerator {
    private static final Logger logger = LoggerFactory.getLogger(ConcurrentGenerator.class);
    private final ExecutorService executorService;
    private final List<LLMClient> llmClients;

    /**
     * Instantiates a new Concurrent generator.
     *
     * @param provider     the provider
     * @param numInstances the num instances
     */
    public ConcurrentGenerator(String provider, int numInstances) {
        this.executorService = Executors.newFixedThreadPool(numInstances);
        this.llmClients = new ArrayList<>();
        
        // Create multiple LLM clients
        for (int i = 0; i < numInstances; i++) {
            llmClients.add(LLMClientFactory.createClient(provider));
        }
    }

    /**
     * Generate all.
     *
     * @param stack      the stack
     * @param outputPath the output path
     * @throws IOException the io exception
     */
    public void generateAll(Stack stack, Path outputPath) throws IOException {
        // Create all necessary directories upfront
        logger.info("Creating output directories");
        Files.createDirectories(outputPath);
        Files.createDirectories(outputPath.resolve(".github/workflows"));
        Files.createDirectories(outputPath.resolve("terraform"));
        
        List<CompletableFuture<Void>> futures = new ArrayList<>();

        // Generate Dockerfile using first client
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                DockerfileGenerator dockerfileGenerator = new DockerfileGenerator(llmClients.get(0));
                dockerfileGenerator.generateDockerfile(stack, outputPath);
            } catch (IOException e) {
                logger.error("Error generating Dockerfile", e);
            }
        }, executorService));

        // Generate Docker Compose using second client
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                DockerComposeGenerator composeGenerator = new DockerComposeGenerator(llmClients.get(1));
                composeGenerator.generateDockerCompose(stack, outputPath);
            } catch (IOException e) {
                logger.error("Error generating Docker Compose", e);
            }
        }, executorService));

        // Generate GitHub Actions using third client
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                GitHubActionsGenerator actionsGenerator = new GitHubActionsGenerator(llmClients.get(2));
                actionsGenerator.generateWorkflows(stack, outputPath.resolve(".github/workflows"));
            } catch (IOException e) {
                logger.error("Error generating GitHub Actions", e);
            }
        }, executorService));

        // Generate Terraform using fourth client
        futures.add(CompletableFuture.runAsync(() -> {
            try {
                TerraformGenerator terraformGenerator = new TerraformGenerator(llmClients.get(3), stack.getCloudProviders().get(0));
                terraformGenerator.generateTerraform(stack, outputPath.resolve("terraform"));
            } catch (IOException e) {
                logger.error("Error generating Terraform", e);
            }
        }, executorService));

        // Wait for all generations to complete
        CompletableFuture.allOf(futures.toArray(new CompletableFuture[0])).join();
        logger.info("All infrastructure files generated successfully in: {}", outputPath);
    }

    /**
     * Shutdown.
     */
    public void shutdown() {
        executorService.shutdown();
    }
} 