package com.odin.model;

import java.util.List;
import java.util.Map;
import java.util.Optional;

/**
 * Represents a complete software stack configuration.
 * This record holds all the necessary information about a project's
 * technology stack, including programming language, framework, databases,
 * and deployment configurations.
 * 
 * The Stack class is immutable and uses the Builder pattern for construction.
 * It's used throughout the application to generate appropriate infrastructure
 * and deployment configurations.
 */
public record Stack(
    String language,          // Primary programming language (e.g., "java", "python")
    String framework,         // Web framework if applicable (e.g., "spring", "django")
    List<Database> databases, // List of databases used by the application
    int runtimePort,         // Port the application runs on
    Map<String, String> environmentVariables, // Environment variables needed
    List<String> dependencies, // Project dependencies
    String buildTool,        // Build tool used (e.g., "maven", "gradle")
    Optional<String> mainClass, // Main class for Java applications
    Optional<String> entryPoint, // Entry point for other languages
    String awsTarget         // Target AWS service (e.g., "ECS", "Lambda")
) {
    /**
     * Represents a database configuration in the stack.
     * Contains all necessary information to set up and connect to a database.
     */
    public record Database(
        String type,          // Database type (e.g., "postgresql", "mongodb")
        String version,       // Database version
        int port,            // Port the database runs on
        Map<String, String> configuration // Additional database configuration
    ) {}

    /**
     * Builder class for constructing Stack instances.
     * Provides a fluent interface for setting stack properties and
     * handles default values for optional fields.
     */
    public static class Builder {
        private String language;
        private String framework;
        private List<Database> databases;
        private int runtimePort = 8080;
        private Map<String, String> environmentVariables;
        private List<String> dependencies;
        private String buildTool;
        private String mainClass;
        private String entryPoint;
        private String awsTarget = "ECS"; // Default to ECS

        /**
         * Sets the primary programming language of the stack.
         * @param language The programming language name
         * @return this builder for method chaining
         */
        public Builder language(String language) {
            this.language = language;
            return this;
        }

        /**
         * Sets the web framework used in the stack.
         * @param framework The framework name
         * @return this builder for method chaining
         */
        public Builder framework(String framework) {
            this.framework = framework;
            return this;
        }

        /**
         * Sets the list of databases used by the application.
         * @param databases List of database configurations
         * @return this builder for method chaining
         */
        public Builder databases(List<Database> databases) {
            this.databases = databases;
            return this;
        }

        /**
         * Sets the runtime port for the application.
         * @param port The port number
         * @return this builder for method chaining
         */
        public Builder runtimePort(int port) {
            this.runtimePort = port;
            return this;
        }

        /**
         * Sets environment variables needed by the application.
         * @param envVars Map of environment variable names to values
         * @return this builder for method chaining
         */
        public Builder environmentVariables(Map<String, String> envVars) {
            this.environmentVariables = envVars;
            return this;
        }

        /**
         * Sets the list of project dependencies.
         * @param dependencies List of dependency identifiers
         * @return this builder for method chaining
         */
        public Builder dependencies(List<String> dependencies) {
            this.dependencies = dependencies;
            return this;
        }

        /**
         * Sets the build tool used by the project.
         * @param buildTool The build tool name
         * @return this builder for method chaining
         */
        public Builder buildTool(String buildTool) {
            this.buildTool = buildTool;
            return this;
        }

        /**
         * Sets the main class for Java applications.
         * @param mainClass The fully qualified main class name
         * @return this builder for method chaining
         */
        public Builder mainClass(String mainClass) {
            this.mainClass = mainClass;
            return this;
        }

        /**
         * Sets the entry point for non-Java applications.
         * @param entryPoint The entry point identifier
         * @return this builder for method chaining
         */
        public Builder entryPoint(String entryPoint) {
            this.entryPoint = entryPoint;
            return this;
        }

        /**
         * Sets the target AWS service for deployment.
         * @param awsTarget The AWS service name
         * @return this builder for method chaining
         */
        public Builder awsTarget(String awsTarget) {
            this.awsTarget = awsTarget;
            return this;
        }

        /**
         * Builds and returns a new Stack instance with the configured properties.
         * @return A new Stack instance
         */
        public Stack build() {
            return new Stack(
                language,
                framework,
                databases,
                runtimePort,
                environmentVariables,
                dependencies,
                buildTool,
                Optional.ofNullable(mainClass),
                Optional.ofNullable(entryPoint),
                awsTarget
            );
        }
    }
} 