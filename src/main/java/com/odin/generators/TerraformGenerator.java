package com.odin.generators;

import com.odin.detection.Stack;
import com.odin.llm.LLMClient;
import com.odin.llm.LLMClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

/**
 * The type Terraform generator.
 */
public class TerraformGenerator {
    private static final Logger logger = LoggerFactory.getLogger(TerraformGenerator.class);
    private final LLMClient llmClient;
    private String cloudProvider = "aws"; // Default to AWS

    /**
     * Instantiates a new Terraform generator.
     *
     * @param provider the provider
     */
    public TerraformGenerator(String provider) {
        this.llmClient = LLMClientFactory.createClient(provider);
    }

    /**
     * Instantiates a new Terraform generator.
     *
     * @param provider      the provider
     * @param cloudProvider the cloud provider
     */
    public TerraformGenerator(String provider, String cloudProvider) {
        this.llmClient = LLMClientFactory.createClient(provider);
        this.cloudProvider = cloudProvider.toLowerCase();
    }

    /**
     * Instantiates a new Terraform generator.
     *
     * @param llmClient     the llm client
     * @param cloudProvider the cloud provider
     */
    public TerraformGenerator(LLMClient llmClient, String cloudProvider) {
        this.llmClient = llmClient;
        this.cloudProvider = cloudProvider.toLowerCase();
    }

    /**
     * Generate terraform.
     *
     * @param stack     the stack
     * @param outputDir the output dir
     * @throws IOException the io exception
     */
    public void generateTerraform(Stack stack, Path outputDir) throws IOException {
        logger.info("Generating Terraform configuration for {} {} application on {}", 
                   stack.getLanguage(), stack.getFramework(), cloudProvider);
        
        // Create output directory if it doesn't exist
        Files.createDirectories(outputDir);
        
        // Generate main.tf
        Path mainTfPath = outputDir.resolve("main.tf");
        generateMainTf(stack, mainTfPath);
        
        // Generate variables.tf
        Path variablesTfPath = outputDir.resolve("variables.tf");
        generateVariablesTf(stack, variablesTfPath);
        
        // Generate outputs.tf
        Path outputsTfPath = outputDir.resolve("outputs.tf");
        generateOutputsTf(stack, outputsTfPath);
        
        // Generate provider.tf
        Path providerTfPath = outputDir.resolve("provider.tf");
        generateProviderTf(stack, providerTfPath);
        
        logger.info("Generated Terraform configuration in: {}", outputDir);
    }
    
    private void generateMainTf(Stack stack, Path outputPath) throws IOException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a Terraform main.tf file for a ").append(stack.getLanguage())
              .append(" application using ").append(stack.getFramework())
              .append(" framework running on port ").append(stack.getAppPort());

        if (!stack.getDatabases().isEmpty()) {
            prompt.append(" with the following databases: ");
            prompt.append(String.join(", ", stack.getDatabases()));
            
            // Add port information for each database
            prompt.append("\nDatabase ports: ");
            for (String db : stack.getDatabases()) {
                prompt.append(db).append("=").append(stack.getDatabasePort(db)).append(", ");
            }
        }

        if ("aws".equals(cloudProvider)) {
            prompt.append("\nThe configuration should deploy the application to AWS ECS Fargate.\n");
            prompt.append("Include all necessary resources: ECS cluster, task definition, service, load balancer, etc.\n");
        } else if ("gcp".equals(cloudProvider)) {
            prompt.append("\nThe configuration should deploy the application to Google Cloud Run.\n");
            prompt.append("Include all necessary resources: Cloud Run service, Cloud SQL (if needed), etc.\n");
        }
        
        prompt.append("Format the output as valid HCL without additional explanations.\n");
        prompt.append("Set appropriate timeouts for all resources (at least 10 minutes for ECS tasks).\n");

        // Use a longer timeout for Terraform generation
        Map<String, Object> params = Map.of("timeout", 300); // 5 minutes timeout
        String mainTfContent = llmClient.generateInfrastructureCode(prompt.toString(), "terraform");
        Files.writeString(outputPath, mainTfContent);
        logger.info("Generated main.tf at: {}", outputPath);
    }
    
    private void generateVariablesTf(Stack stack, Path outputPath) throws IOException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a Terraform variables.tf file for a ").append(stack.getLanguage())
              .append(" application using ").append(stack.getFramework())
              .append(" framework on ").append(cloudProvider.toUpperCase()).append(".\n");
        
        prompt.append("Include variables for:\n");
        if ("aws".equals(cloudProvider)) {
            prompt.append("- AWS region\n");
        } else if ("gcp".equals(cloudProvider)) {
            prompt.append("- GCP project ID\n");
            prompt.append("- GCP region\n");
        }
        prompt.append("- Environment (dev, staging, prod)\n");
        prompt.append("- Application name\n");
        prompt.append("- Container port (default: ").append(stack.getAppPort()).append(")\n");
        
        if (!stack.getDatabases().isEmpty()) {
            prompt.append("- Database configurations\n");
        }
        
        prompt.append("Format the output as valid HCL without additional explanations.\n");

        String variablesTfContent = llmClient.generateInfrastructureCode(prompt.toString(), "terraform");
        Files.writeString(outputPath, variablesTfContent);
        logger.info("Generated variables.tf at: {}", outputPath);
    }
    
    private void generateOutputsTf(Stack stack, Path outputPath) throws IOException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a Terraform outputs.tf file for a ").append(stack.getLanguage())
              .append(" application using ").append(stack.getFramework())
              .append(" framework on ").append(cloudProvider.toUpperCase()).append(".\n");
        
        prompt.append("Include outputs for:\n");
        if ("aws".equals(cloudProvider)) {
            prompt.append("- Load balancer DNS name\n");
            prompt.append("- ECS cluster name\n");
            prompt.append("- Service name\n");
        } else if ("gcp".equals(cloudProvider)) {
            prompt.append("- Cloud Run service URL\n");
            prompt.append("- Service name\n");
        }
        prompt.append("Format the output as valid HCL without additional explanations.\n");

        String outputsTfContent = llmClient.generateInfrastructureCode(prompt.toString(), "terraform");
        Files.writeString(outputPath, outputsTfContent);
        logger.info("Generated outputs.tf at: {}", outputPath);
    }
    
    private void generateProviderTf(Stack stack, Path outputPath) throws IOException {
        StringBuilder prompt = new StringBuilder();
        if ("aws".equals(cloudProvider)) {
            prompt.append("Generate a Terraform provider.tf file for AWS.\n");
            prompt.append("Include provider configuration for AWS with appropriate region variable.\n");
        } else if ("gcp".equals(cloudProvider)) {
            prompt.append("Generate a Terraform provider.tf file for Google Cloud Platform.\n");
            prompt.append("Include provider configuration for GCP with appropriate project and region variables.\n");
        }
        prompt.append("Format the output as valid HCL without additional explanations.\n");

        String providerTfContent = llmClient.generateInfrastructureCode(prompt.toString(), "terraform");
        Files.writeString(outputPath, providerTfContent);
        logger.info("Generated provider.tf at: {}", outputPath);
    }
} 