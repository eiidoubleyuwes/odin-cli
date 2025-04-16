package com.odin.generators;

import com.odin.detection.Stack;
import com.odin.llm.LLMClientFactory;
import com.odin.llm.LLMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

/**
 * Generates Docker Compose configurations for multi-container applications.
 * 
 * This class uses AI to generate optimized docker-compose.yml files based on the detected
 * technology stack. It handles:
 * - Application service configuration
 * - Database service setup
 * - Network configuration
 * - Volume management
 * - Health checks
 * - Environment variables
 */
public class DockerComposeGenerator {
    private static final Logger logger = LoggerFactory.getLogger(DockerComposeGenerator.class);
    private final LLMClient client;

    /**
     * Creates a new DockerComposeGenerator with specified LLM provider.
     * 
     * @param provider The LLM provider to use (e.g., "ollama", "gemini")
     */
    public DockerComposeGenerator(String provider) {
        this.client = LLMClientFactory.createClient(provider);
    }
    
    /**
     * Creates a new DockerComposeGenerator with existing LLM client.
     * 
     * @param client The LLM client to use for generation
     */
    public DockerComposeGenerator(LLMClient client) {
        this.client = client;
    }

    /**
     * Generates a docker-compose.yml file for the specified stack.
     * 
     * @param stack The detected technology stack
     * @param outputPath Where to save the generated docker-compose.yml
     * @throws IOException if there are file system access issues
     */
    public void generateDockerCompose(Stack stack, Path outputPath) throws IOException {
        logger.info("Generating docker-compose.yml for stack: {}", stack);
        
        // Build the prompt for the LLM
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a docker-compose.yml file for a ").append(stack.getLanguage())
              .append(" application using ").append(stack.getFramework())
              .append(" framework running on port ").append(stack.getAppPort());

        // Add database configuration if present
        if (!stack.getDatabases().isEmpty()) {
            prompt.append(" with the following databases: ");
            prompt.append(String.join(", ", stack.getDatabases()));
            
            // Add port information for each database
            prompt.append("\nDatabase ports: ");
            for (String db : stack.getDatabases()) {
                prompt.append(db).append("=").append(stack.getDatabasePort(db)).append(", ");
            }
        }

        // Add requirements for the compose file
        prompt.append("\nInclude environment variables and volume mounts as needed.\n");
        prompt.append("The compose file should follow best practices and include appropriate healthchecks.\n");
        prompt.append("Format the output as valid YAML without additional explanations.\n");
        prompt.append("IMPORTANT: Do not include any Dockerfile content in the output. The Dockerfile should be a separate file.\n");
        prompt.append("The output should only contain the docker-compose.yml configuration.\n");

        String composeContent = client.generateInfrastructureCode(prompt.toString(), "docker-compose");
        Path composePath = outputPath.resolve("docker-compose.yml");
        Files.writeString(composePath, composeContent);
        logger.info("Generated docker-compose.yml at: {}", composePath);
    }

    /**
     * Gets a base template for the docker-compose.yml file.
     * 
     * @param stack The detected technology stack
     * @return Base template string for the compose file
     */
    private String getBaseTemplate(Stack stack) {
        StringBuilder template = new StringBuilder("""
            version: '3.8'
            
            services:
              app:
                build:
                  context: .
                  dockerfile: Dockerfile
                ports:
                  - "${PORT:-8080}:8080"
                environment:
                  - NODE_ENV=development
            """);

        // Add database services
        if (!stack.getDatabases().isEmpty()) {
            template.append("\n  # Database Services\n");
            for (String db : stack.getDatabases()) {
                template.append(generateDatabaseService(db));
            }
        }

        // Add volumes
        template.append("""
            
            volumes:
              app-data:
              db-data:
            """);

        return template.toString();
    }

    /**
     * Generates a service configuration for a specific database type.
     * 
     * @param dbType The type of database (e.g., "postgres", "mongodb")
     * @return Service configuration string for the database
     */
    private String generateDatabaseService(String dbType) {
        return switch (dbType.toLowerCase()) {
            case "postgresql" -> String.format("""
                
              postgres:
                image: postgres:15-alpine
                environment:
                  - POSTGRES_USER=postgres
                  - POSTGRES_PASSWORD=postgres
                  - POSTGRES_DB=app
                ports:
                  - "5432:5432"
                volumes:
                  - db-data:/var/lib/postgresql/data
                """);
                
            case "mysql" -> String.format("""
                
              mysql:
                image: mysql:8.0
                environment:
                  - MYSQL_ROOT_PASSWORD=root
                  - MYSQL_DATABASE=app
                  - MYSQL_USER=app
                  - MYSQL_PASSWORD=app
                ports:
                  - "3306:3306"
                volumes:
                  - db-data:/var/lib/mysql
                """);
                
            case "mongodb" -> String.format("""
                
              mongodb:
                image: mongo:6.0
                environment:
                  - MONGO_INITDB_ROOT_USERNAME=root
                  - MONGO_INITDB_ROOT_PASSWORD=root
                ports:
                  - "27017:27017"
                volumes:
                  - db-data:/data/db
                """);
                
            case "redis" -> String.format("""
                
              redis:
                image: redis:7.0-alpine
                ports:
                  - "6379:6379"
                volumes:
                  - db-data:/data
                """);
                
            default -> "";
        };
    }
} 