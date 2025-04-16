package com.odin.cli;

import picocli.CommandLine;
import picocli.CommandLine.Command;
import picocli.CommandLine.Option;
import picocli.CommandLine.Parameters;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.odin.detection.StackDetector;
import com.odin.detection.Stack;
import com.odin.generators.*;
import com.odin.validators.*;
import com.odin.explainer.Explainer;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.odin.monitoring.DockerMonitor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.concurrent.Callable;
import java.util.HashMap;
import java.util.Map;
import java.nio.file.Paths;
import java.util.List;

/**
 * Main CLI interface for Odin - a Docker container monitoring and management tool.
 * Provides commands for monitoring containers, generating Docker configs, and managing deployments.
 * Uses picocli for command-line argument parsing and subcommand handling.
 */
@Command(
    name = "odin",
    subcommands = {
        OdinCommand.InitCommand.class,
        OdinCommand.DockerCommand.class,
        OdinCommand.ComposeCommand.class,
        OdinCommand.TerraformCommand.class,
        OdinCommand.ActionsCommand.class,
        OdinCommand.AllCommand.class,
        OdinCommand.ValidateCommand.class,
        OdinCommand.ExplainCommand.class,
        OdinCommand.ConfigCommand.class,
        OdinCommand.GitHubCommand.class,
        OdinCommand.MonitorCommand.class
    },
    description = "Docker container monitoring and management tool"
)
public class OdinCommand implements Callable<Integer> {
    private static final Logger logger = LoggerFactory.getLogger(OdinCommand.class);

    public static void main(String[] args) {
        int exitCode = new CommandLine(new OdinCommand()).execute(args);
        System.exit(exitCode);
    }

    @Override
    public Integer call() {
        logger.info("Welcome to Odin! Use --help to see available commands.");
        return 0;
    }

    @Command(
        name = "init",
        description = "Detect and summarize project stack"
    )
    public static class InitCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "Project root directory")
        private Path projectDir;

        @Option(names = "--provider", description = "LLM provider to use (ollama/gemini)")
        private String provider;

        @Override
        public Integer call() throws Exception {
            logger.info("Analyzing project stack in: {}", projectDir);
            
            // Set provider in environment if specified
            if (provider != null) {
                System.setProperty("LLM_PROVIDER", provider);
            }
            
            StackDetector detector = new StackDetector();
            Stack stack = detector.detectStack(projectDir);
            logger.info("Detected stack: {}", stack);
            return 0;
        }
    }

    @Command(
        name = "docker",
        description = "Generate Dockerfile"
    )
    public static class DockerCommand implements Callable<Integer> {
        @Option(names = {"-o", "--output"}, description = "Output directory")
        private Path outputDir;

        @Parameters(index = "0", description = "Project root directory")
        private Path projectDir;

        @Option(names = {"--provider"}, description = "LLM provider to use (ollama/gemini)")
        private String provider = "ollama";

        @Override
        public Integer call() throws IOException {
            logger.info("Generating Dockerfile");
            
            // Create output directory if it doesn't exist
            Path outputPath = outputDir != null ? outputDir : projectDir.resolve("docker");
            Files.createDirectories(outputPath);
            
            StackDetector detector = new StackDetector();
            Stack stack = detector.detectStack(projectDir);
            
            DockerfileGenerator generator = new DockerfileGenerator(provider);
            generator.generateDockerfile(stack, outputPath);
            return 0;
        }
    }

    /**
     * Command for generating Docker Compose configurations.
     * Analyzes project structure and dependencies to create appropriate
     * Docker Compose files with proper service definitions.
     */
    @Command(
        name = "compose",
        description = "Generate Docker Compose configuration"
    )
    public static class ComposeCommand implements Callable<Integer> {
        @Option(names = {"-o", "--output"}, description = "Output directory")
        private Path outputDir;

        @Parameters(index = "0", description = "Project root directory")
        private Path projectDir;

        @Option(names = {"--provider"}, description = "LLM provider to use (ollama/gemini)")
        private String provider = "ollama";

        @Override
        public Integer call() throws IOException {
            logger.info("Generating docker-compose.yml");
            StackDetector detector = new StackDetector();
            Stack stack = detector.detectStack(projectDir);
            
            DockerComposeGenerator generator = new DockerComposeGenerator(provider);
            generator.generateDockerCompose(stack, outputDir.resolve("docker-compose.yml"));
            return 0;
        }
    }

    @Command(
        name = "terraform",
        description = "Generate Terraform configuration"
    )
    public static class TerraformCommand implements Callable<Integer> {
        @Option(names = {"-o", "--output"}, description = "Output directory")
        private Path outputDir;

        @Parameters(index = "0", description = "Project root directory")
        private Path projectDir;

        @Option(names = {"--provider"}, description = "LLM provider to use (ollama/gemini)")
        private String provider = "ollama";
        
        @Option(names = {"--cloud"}, description = "Cloud provider (aws/gcp)")
        private String cloudProvider = "aws";

        @Override
        public Integer call() throws IOException {
            logger.info("Generating Terraform configuration for {} cloud provider", cloudProvider);
            StackDetector detector = new StackDetector();
            Stack stack = detector.detectStack(projectDir);
            
            TerraformGenerator generator = new TerraformGenerator(provider, cloudProvider);
            generator.generateTerraform(stack, outputDir);
            return 0;
        }
    }

    @Command(
        name = "actions",
        description = "Generate GitHub Actions workflow"
    )
    public static class ActionsCommand implements Callable<Integer> {
        @Option(names = {"-o", "--output"}, description = "Output directory")
        private Path outputDir;

        @Parameters(index = "0", description = "Project root directory")
        private Path projectDir;

        @Option(names = {"--provider"}, description = "LLM provider to use (ollama/gemini)")
        private String provider = "ollama";

        @Option(names = {"--cloud"}, description = "Cloud provider (aws/gcp)")
        private String cloudProvider = "aws";

        @Override
        public Integer call() throws IOException {
            logger.info("Generating GitHub Actions workflow for {} cloud provider", cloudProvider);
            StackDetector detector = new StackDetector();
            Stack stack = detector.detectStack(projectDir);
            
            GitHubActionsGenerator generator = new GitHubActionsGenerator(provider, cloudProvider);
            generator.generateWorkflows(stack, outputDir);
            return 0;
        }
    }

    @Command(
        name = "all",
        description = "Generate all infrastructure files"
    )
    public static class AllCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "Path to the application")
        private Path appPath;

        @Option(names = {"-o", "--output"}, description = "Output directory")
        private Path outputDir;

        @Option(names = {"-p", "--provider"}, description = "LLM provider")
        private String provider;

        @Option(names = {"-c", "--cloud"}, description = "Cloud provider")
        private String cloudProvider;

        @Override
        public Integer call() throws IOException {
            try {
                Path outputPath = outputDir != null ? outputDir : appPath.resolve("infrastructure");
                
                // Create a new concurrent generator with 4 LLM instances
                ConcurrentGenerator generator = new ConcurrentGenerator(provider != null ? provider : "ollama", 4);
                
                // Detect the stack
                StackDetector detector = new StackDetector();
                Stack stack = detector.detectStack(appPath);
                
                // Add cloud provider if specified
                if (cloudProvider != null) {
                    stack.getCloudProviders().add(cloudProvider.toLowerCase());
                }
                
                // Generate all infrastructure files concurrently
                generator.generateAll(stack, outputPath);
                generator.shutdown();
                
                logger.info("All infrastructure files generated successfully in: {}", outputPath);
                return 0;
            } catch (Exception e) {
                logger.error("Failed to generate infrastructure files: {}", e.getMessage());
                return 1;
            }
        }
    }

    @Command(
        name = "validate",
        description = "Validate generated infrastructure code"
    )
    public static class ValidateCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "Directory containing generated files")
        private Path directory;

        @Override
        public Integer call() {
            logger.info("Validating infrastructure code in: {}", directory);
            // TODO: Implement validation
            return 0;
        }
    }

    @Command(
        name = "explain",
        description = "Explain generated infrastructure code"
    )
    public static class ExplainCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "File to explain")
        private Path file;

        @Option(names = {"--provider"}, description = "LLM provider to use (ollama/gemini)")
        private String provider = "ollama";

        @Override
        public Integer call() throws IOException {
            logger.info("Explaining file: {}", file);
            String code = Files.readString(file);
            Explainer explainer = new Explainer(provider);
            String explanation = explainer.explainCode(code);
            System.out.println(explanation);
            return 0;
        }
    }

    @Command(
        name = "config",
        description = "Configure Odin settings"
    )
    public static class ConfigCommand implements Callable<Integer> {
        @Option(names = {"--provider"}, description = "Default LLM provider (ollama/gemini)")
        private String provider;

        @Option(names = {"--cloud"}, description = "Default cloud provider (aws/gcp)")
        private String cloudProvider;

        @Option(names = {"--threads"}, description = "Default number of threads for concurrent generation")
        private Integer threads;

        @Option(names = {"--timeout"}, description = "Default timeout for LLM requests in seconds")
        private Integer timeout;

        @Option(names = {"--output-dir"}, description = "Default output directory")
        private Path outputDir;

        @Override
        public Integer call() throws IOException {
            logger.info("Configuring Odin settings");
            
            // Create config directory if it doesn't exist
            Path configDir = Path.of(System.getProperty("user.home"), ".odin");
            Files.createDirectories(configDir);
            
            // Read existing config if it exists
            Path configFile = configDir.resolve("config.json");
            Map<String, Object> config = new HashMap<>();
            
            if (Files.exists(configFile)) {
                try {
                    config = new ObjectMapper().readValue(configFile.toFile(), Map.class);
                } catch (IOException e) {
                    logger.warn("Failed to read existing config: {}", e.getMessage());
                }
            }
            
            // Update config with new values
            if (provider != null) {
                config.put("provider", provider);
                System.setProperty("ODIN_PROVIDER", provider);
            }
            
            if (cloudProvider != null) {
                config.put("cloud", cloudProvider);
                System.setProperty("ODIN_CLOUD", cloudProvider);
            }
            
            if (threads != null) {
                config.put("threads", threads);
                System.setProperty("ODIN_THREADS", threads.toString());
            }
            
            if (timeout != null) {
                config.put("timeout", timeout);
                System.setProperty("ODIN_TIMEOUT", timeout.toString());
            }
            
            if (outputDir != null) {
                config.put("output_dir", outputDir.toString());
                System.setProperty("ODIN_OUTPUT_DIR", outputDir.toString());
            }
            
            // Write updated config
            try {
                new ObjectMapper()
                    .writerWithDefaultPrettyPrinter()
                    .writeValue(configFile.toFile(), config);
                logger.info("Configuration saved to: {}", configFile);
            } catch (IOException e) {
                logger.error("Failed to save configuration: {}", e.getMessage());
                return 1;
            }
            
            // Display current configuration
            logger.info("Current configuration:");
            config.forEach((key, value) -> logger.info("  {}: {}", key, value));
            
            return 0;
        }
    }

    @Command(
        name = "github",
        description = "Generate GitHub Actions workflows"
    )
    public static class GitHubCommand implements Callable<Integer> {
        @Parameters(index = "0", description = "Project root directory")
        private Path projectDir;

        @Option(names = {"-o", "--output"}, description = "Output directory")
        private Path outputDir = Path.of("output");

        @Option(names = {"--provider"}, description = "LLM provider to use (ollama/gemini)")
        private String provider = "ollama";

        @Override
        public Integer call() throws IOException {
            logger.info("Generating GitHub Actions workflows");
            StackDetector detector = new StackDetector();
            Stack stack = detector.detectStack(projectDir);
            
            GitHubActionsGenerator generator = new GitHubActionsGenerator(provider);
            generator.generateWorkflows(stack, outputDir);
            logger.info("GitHub Actions workflows generated successfully!");
            return 0;
        }
    }

    /**
     * Command for monitoring Docker containers in real-time.
     * Displays container stats, logs, and AI-powered analysis of container health.
     * Updates automatically at configurable intervals.
     */
    @Command(
        name = "monitor",
        description = "Monitor Docker containers"
    )
    public static class MonitorCommand implements Callable<Integer> {
        @Option(names = {"-i", "--interval"}, description = "Monitoring interval in seconds")
        private int interval = 30;

        @Option(names = {"--provider"}, description = "LLM provider to use (ollama/gemini)")
        private String provider = "ollama";

        @Override
        public Integer call() throws IOException {
            logger.info("Starting Docker container monitoring");
            
            // Set provider in environment if specified
            if (provider != null) {
                System.setProperty("LLM_PROVIDER", provider);
            }
            
            DockerMonitor monitor = new DockerMonitor();
            monitor.startMonitoring();
            
            // Keep the main thread alive
            try {
                while (true) {
                    Thread.sleep(1000);
                    
                    // Print current stats
                    Map<String, DockerMonitor.ContainerStats> stats = monitor.getContainerStats();
                    Map<String, List<String>> failures = monitor.getFailurePatterns();
                    
                    if (!stats.isEmpty()) {
                        System.out.println("\n=== Container Statistics ===");
                        stats.forEach((containerId, containerStats) -> {
                            System.out.printf("Container: %s%n", containerId);
                            System.out.printf("  CPU Usage: %d%%%n", containerStats.getCpuUsage());
                            System.out.printf("  Memory Usage: %d/%d MB%n", 
                                containerStats.getMemoryUsage() / 1024 / 1024,
                                containerStats.getMemoryLimit() / 1024 / 1024);
                            System.out.printf("  Network: RX=%d KB, TX=%d KB%n",
                                containerStats.getNetworkRx() / 1024,
                                containerStats.getNetworkTx() / 1024);
                        });
                    }
                    
                    if (!failures.isEmpty()) {
                        System.out.println("\n=== Detected Issues ===");
                        failures.forEach((containerId, failureList) -> {
                            System.out.printf("Container: %s%n", containerId);
                            failureList.forEach(failure -> System.out.printf("  - %s%n", failure));
                        });
                    }
                }
            } catch (InterruptedException e) {
                monitor.stopMonitoring();
                Thread.currentThread().interrupt();
                return 1;
            }
        }
    }
} 