package com.odin.generators;

import com.odin.detection.Stack;
import com.odin.llm.LLMClientFactory;
import com.odin.llm.LLMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Generates Dockerfile configurations for different application stacks.
 * 
 * This class uses AI to generate optimized Dockerfiles based on the detected
 * technology stack. It considers:
 * - Programming language and framework
 * - Database dependencies
 * - Build tools and requirements
 * - Security best practices
 * - Multi-stage build optimization
 */
public class DockerfileGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DockerfileGenerator.class);
    private final LLMClient client;

    /**
     * Creates a new DockerfileGenerator with specified LLM provider.
     * 
     * @param provider The LLM provider to use (e.g., "ollama", "gemini")
     */
    public DockerfileGenerator(String provider) {
        this.client = LLMClientFactory.createClient(provider);
    }
    
    /**
     * Creates a new DockerfileGenerator with existing LLM client.
     * 
     * @param client The LLM client to use for generation
     */
    public DockerfileGenerator(LLMClient client) {
        this.client = client;
    }

    /**
     * Generates a Dockerfile for the specified stack.
     * 
     * @param stack The detected technology stack
     * @param outputPath Where to save the generated Dockerfile
     * @throws IOException if there are file system access issues
     */
    public void generateDockerfile(Stack stack, Path outputPath) throws IOException {
        logger.info("Generating Dockerfile for {} {} application", stack.getLanguage(), stack.getFramework());
        
        // Determine the correct port based on the language
        int port = determinePort(stack.getLanguage());
        
        // Build the prompt for the LLM
        StringBuilder prompt = new StringBuilder();
        prompt.append("You are an expert DevOps engineer. Generate a complete Dockerfile for a ")
              .append(stack.getLanguage())
              .append(" application using ")
              .append(stack.getFramework())
              .append(" framework running on port ")
              .append(port);

        // Add database information if present
        if (!stack.getDatabases().isEmpty()) {
            prompt.append(" with the following databases: ");
            prompt.append(String.join(", ", stack.getDatabases()));
        }

        // Add requirements for the Dockerfile
        prompt.append("\n\nRequirements:\n");
        prompt.append("1. Use a multi-stage build for smaller final image\n");
        prompt.append("2. Include all necessary dependencies and build steps\n");
        prompt.append(getLanguageSpecificRequirements(stack.getLanguage()));
        prompt.append("4. Set up proper working directory and environment variables\n");
        prompt.append("5. Include healthcheck and proper CMD/ENTRYPOINT\n");
        prompt.append("6. Follow security best practices:\n");
        prompt.append("   - Create and use a non-root user (e.g., appuser)\n");
        prompt.append("   - Set proper file permissions (e.g., 755 for directories, 644 for files)\n");
        prompt.append("   - Use secure base images and keep them updated\n");
        prompt.append("   - Minimize installed packages and remove build dependencies\n");
        prompt.append("7. Optimize layer caching:\n");
        prompt.append("   - Copy dependency files first (requirements.txt, package.json, etc.)\n");
        prompt.append("   - Install dependencies before copying application code\n");
        prompt.append("   - Use .dockerignore for unnecessary files\n");
        prompt.append("8. Copy only necessary files from builder stage\n");
        prompt.append("9. Set appropriate environment variables:\n");
        if (stack.getDatabases().contains("postgresql")) {
            prompt.append("   - POSTGRES_HOST=postgres\n");
            prompt.append("   - POSTGRES_PORT=5432\n");
            prompt.append("   - POSTGRES_DB=app\n");
            prompt.append("   - POSTGRES_USER=postgres\n");
            prompt.append("   - POSTGRES_PASSWORD=postgres\n");
        }
        if (stack.getDatabases().contains("redis")) {
            prompt.append("   - REDIS_HOST=redis\n");
            prompt.append("   - REDIS_PORT=6379\n");
        }
        prompt.append("10. Include proper EXPOSE statement for port ").append(port).append("\n");
        prompt.append("11. Use gunicorn or uvicorn for production-ready server\n\n");
        prompt.append("Format the output as a valid Dockerfile without additional explanations.\n\n");
        prompt.append("IMPORTANT: Return ONLY the raw code without any explanations, comments, markdown formatting, or code block markers.\n");
        prompt.append("The code should be immediately executable without any modifications.");

        String dockerfileContent = client.generateInfrastructureCode(prompt.toString(), "dockerfile");
        logger.info("Generated Dockerfile content length: {}", dockerfileContent.length());
        logger.info("Generated Dockerfile content: \n{}", dockerfileContent);
        
        // Ensure the output directory exists
        Files.createDirectories(outputPath);
        
        // Write the Dockerfile directly to the output path using a BufferedWriter
        Path dockerfilePath = outputPath.resolve("Dockerfile");
        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(dockerfilePath)) {
            writer.write(dockerfileContent);
            writer.flush();
        }
        logger.info("Generated Dockerfile at: {}", dockerfilePath);
        
        // Write a .dockerignore file
        String dockerignoreContent = generateDockerignore(stack);
        Path dockerignorePath = outputPath.resolve(".dockerignore");
        try (java.io.BufferedWriter writer = java.nio.file.Files.newBufferedWriter(dockerignorePath)) {
            writer.write(dockerignoreContent);
            writer.flush();
        }
        logger.info("Generated .dockerignore at: {}", dockerignorePath);
    }
    
    /**
     * Determines the default port for a given programming language.
     * 
     * @param language The programming language
     * @return The default port number for the language
     */
    private int determinePort(String language) {
        switch (language.toLowerCase()) {
            case "python":
                return 5000;
            case "node":
            case "javascript":
                return 3000;
            case "java":
                return 8080;
            case "go":
                return 8080;
            default:
                return 3000;
        }
    }
    
    /**
     * Gets language-specific requirements for the Dockerfile.
     * 
     * @param language The programming language
     * @return String containing language-specific requirements
     */
    private String getLanguageSpecificRequirements(String language) {
        switch (language.toLowerCase()) {
            case "python":
                return "3. Use Python 3.9-slim as the base image (not Alpine)\n";
            case "node":
            case "javascript":
                return "3. Use Node.js LTS slim as the base image (not Alpine)\n";
            case "java":
                return "3. Use Eclipse Temurin JDK slim as the base image (not Alpine)\n";
            case "go":
                return "3. Use golang:latest for build stage and distroless for runtime\n";
            case "rust":
                return "3. Use rust:slim for build stage and debian:slim for runtime\n";
            default:
                return "3. Use an appropriate slim base image for the runtime\n";
        }
    }
    
    /**
     * Generates a .dockerignore file for the project.
     * 
     * @param stack The detected technology stack
     * @return Contents of the .dockerignore file
     */
    private String generateDockerignore(Stack stack) {
        StringBuilder dockerignore = new StringBuilder();
        dockerignore.append("# Version control\n");
        dockerignore.append(".git\n");
        dockerignore.append(".gitignore\n\n");
        
        dockerignore.append("# IDE files\n");
        dockerignore.append(".idea/\n");
        dockerignore.append(".vscode/\n");
        dockerignore.append("*.iml\n\n");
        
        dockerignore.append("# Build outputs\n");
        dockerignore.append("target/\n");
        dockerignore.append("dist/\n");
        dockerignore.append("build/\n");
        dockerignore.append("*.class\n");
        dockerignore.append("*.jar\n");
        dockerignore.append("*.war\n");
        dockerignore.append("*.ear\n\n");
        
        dockerignore.append("# Dependencies\n");
        dockerignore.append("node_modules/\n");
        dockerignore.append("venv/\n");
        dockerignore.append("env/\n");
        dockerignore.append(".env\n\n");
        
        dockerignore.append("# Logs\n");
        dockerignore.append("*.log\n");
        dockerignore.append("logs/\n\n");
        
        dockerignore.append("# Test files\n");
        dockerignore.append("test/\n");
        dockerignore.append("tests/\n");
        dockerignore.append("__tests__/\n");
        dockerignore.append("*.test.js\n");
        dockerignore.append("*.spec.js\n");
        
        return dockerignore.toString();
    }
}