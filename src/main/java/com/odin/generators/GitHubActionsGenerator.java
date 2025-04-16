package com.odin.generators;

import com.odin.detection.Stack;
import com.odin.llm.LLMClient;
import com.odin.llm.LLMClientFactory;
import com.odin.analysis.PerformanceAnalyzer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;

public class GitHubActionsGenerator {
    private static final Logger logger = LoggerFactory.getLogger(GitHubActionsGenerator.class);
    private final LLMClient llmClient;
    private final PerformanceAnalyzer performanceAnalyzer;
    private String cloudProvider = "aws"; // Default to AWS

    // Enhanced MCP parameters for better output quality and performance analysis
    private static final Map<String, Object> MCP_PARAMS = Map.of(
        "temperature", 0.7, //Controls Randomness
        "top_p", 0.9, //Works together with temeprature ie nucleus sampling
        "frequency_penalty", 0.5,
        "presence_penalty", 0.5, //To encourage new topics
        "max_tokens", 2000,
        "performance_metrics", Map.of(
            "throughput_threshold", 1000,   // requests per second
            "error_rate_threshold", 0.01,   // 1% error rate
            "cpu_threshold", 80,            // percentage
            "memory_threshold", 85,         // percentage
            "latency_threshold", 100,       // milliseconds
            "concurrency_threshold", 100    // concurrent users
        ),
        "analysis_depth", "comprehensive",
        "optimization_level", "high",
        "monitoring_granularity", "detailed"
    );

    public GitHubActionsGenerator(String provider) {
        this.llmClient = LLMClientFactory.createClient(provider);
        this.performanceAnalyzer = new PerformanceAnalyzer(provider);
    }
    
    public GitHubActionsGenerator(LLMClient llmClient) {
        this.llmClient = llmClient;
        this.performanceAnalyzer = new PerformanceAnalyzer(llmClient);
    }

    public GitHubActionsGenerator(String provider, String cloudProvider) {
        this.llmClient = LLMClientFactory.createClient(provider);
        this.performanceAnalyzer = new PerformanceAnalyzer(provider);
        this.cloudProvider = cloudProvider.toLowerCase();
    }
    
    public GitHubActionsGenerator(LLMClient llmClient, String cloudProvider) {
        this.llmClient = llmClient;
        this.performanceAnalyzer = new PerformanceAnalyzer(llmClient);
        this.cloudProvider = cloudProvider.toLowerCase();
    }

    public void generateWorkflows(Stack stack, Path outputDir) throws IOException {
        logger.info("Generating GitHub Actions workflows for {} {} application on {}", 
                   stack.getLanguage(), stack.getFramework(), cloudProvider);
        
        // Create output directory if it doesn't exist
        Files.createDirectories(outputDir);
        
        // Generate CI workflow
        Path ciWorkflowPath = outputDir.resolve("ci.yml");
        generateCIWorkflow(stack, ciWorkflowPath);
        
        // Generate CD workflow
        Path cdWorkflowPath = outputDir.resolve("cd.yml");
        generateCDWorkflow(stack, cdWorkflowPath);
        
        // Generate test workflow
        Path testWorkflowPath = outputDir.resolve("test.yml");
        generateTestWorkflow(stack, testWorkflowPath);
        
        // Generate security workflow
        Path securityWorkflowPath = outputDir.resolve("security.yml");
        generateSecurityWorkflow(stack, securityWorkflowPath);
        
        logger.info("Generated GitHub Actions workflows in: {}", outputDir);
    }
    
    private void generateCIWorkflow(Stack stack, Path outputPath) throws IOException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a GitHub Actions CI workflow for a ").append(stack.getLanguage())
              .append(" application using ").append(stack.getFramework())
              .append(" framework.\n");
        
        prompt.append("The workflow should:\n");
        prompt.append("- Run on push to main and pull requests\n");
        prompt.append("- Set a timeout of at least 30 minutes\n");
        prompt.append("- Include steps for linting, building, and testing\n");
        prompt.append("- Use appropriate caching strategies\n");
        prompt.append("- Format the output as valid YAML without additional explanations.\n");

        // Use a longer timeout for workflow generation
        Map<String, Object> params = Map.of("timeout", 240); // 4 minutes timeout
        String ciWorkflowContent = llmClient.generateInfrastructureCode(prompt.toString(), "github-actions");
        Files.writeString(outputPath, ciWorkflowContent);
        logger.info("Generated CI workflow at: {}", outputPath);
    }
    
    private void generateCDWorkflow(Stack stack, Path outputPath) throws IOException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a GitHub Actions CD workflow for a ").append(stack.getLanguage())
              .append(" application using ").append(stack.getFramework())
              .append(" framework.\n");
        
        prompt.append("The workflow should:\n");
        prompt.append("- Run on push to main and tags\n");
        prompt.append("- Set a timeout of at least 45 minutes\n");
        
        if ("aws".equals(cloudProvider)) {
            prompt.append("- Include steps for building, testing, and deploying to AWS ECS\n");
            prompt.append("- Use AWS credentials and ECS deployment steps\n");
        } else if ("gcp".equals(cloudProvider)) {
            prompt.append("- Include steps for building, testing, and deploying to Google Cloud Run\n");
            prompt.append("- Use Google Cloud credentials and Cloud Run deployment steps\n");
        }
        
        prompt.append("- Use appropriate environment secrets\n");
        prompt.append("- Format the output as valid YAML without additional explanations.\n");

        // Use a longer timeout for workflow generation
        Map<String, Object> params = Map.of("timeout", 240); // 4 minutes timeout
        String cdWorkflowContent = llmClient.generateInfrastructureCode(prompt.toString(), "github-actions");
        Files.writeString(outputPath, cdWorkflowContent);
        logger.info("Generated CD workflow at: {}", outputPath);
    }
    
    private void generateTestWorkflow(Stack stack, Path outputPath) throws IOException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a GitHub Actions test workflow for a ").append(stack.getLanguage())
              .append(" application using ").append(stack.getFramework())
              .append(" framework.\n");
        
        prompt.append("The workflow should:\n");
        prompt.append("- Run on push to main and pull requests\n");
        prompt.append("- Set a timeout of at least 20 minutes\n");
        prompt.append("- Include steps for running unit tests, integration tests, and e2e tests\n");
        prompt.append("- Use appropriate test frameworks for ").append(stack.getLanguage()).append("\n");
        prompt.append("- Format the output as valid YAML without additional explanations.\n");

        // Use a longer timeout for workflow generation
        Map<String, Object> params = Map.of("timeout", 180); // 3 minutes timeout
        String testWorkflowContent = llmClient.generateInfrastructureCode(prompt.toString(), "github-actions");
        Files.writeString(outputPath, testWorkflowContent);
        logger.info("Generated test workflow at: {}", outputPath);
    }
    
    private void generateSecurityWorkflow(Stack stack, Path outputPath) throws IOException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a GitHub Actions security workflow for a ").append(stack.getLanguage())
              .append(" application using ").append(stack.getFramework())
              .append(" framework.\n");
        
        prompt.append("The workflow should:\n");
        prompt.append("- Run on a schedule (weekly) and on push to main\n");
        prompt.append("- Set a timeout of at least 15 minutes\n");
        prompt.append("- Include steps for dependency scanning, SAST, and container scanning\n");
        
        if ("aws".equals(cloudProvider)) {
            prompt.append("- Include AWS-specific security checks and compliance scanning\n");
        } else if ("gcp".equals(cloudProvider)) {
            prompt.append("- Include GCP-specific security checks and compliance scanning\n");
        }
        
        prompt.append("- Format the output as valid YAML without additional explanations.\n");

        // Use a longer timeout for workflow generation
        Map<String, Object> params = Map.of("timeout", 180); // 3 minutes timeout
        String securityWorkflowContent = llmClient.generateInfrastructureCode(prompt.toString(), "github-actions");
        Files.writeString(outputPath, securityWorkflowContent);
        logger.info("Generated security workflow at: {}", outputPath);
    }
} 