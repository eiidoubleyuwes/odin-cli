package com.odin.detection;

import com.odin.llm.LLMClient;
import com.odin.llm.LLMClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;
import java.util.stream.IntStream;
import static java.util.Map.entry;

/**
 * Detects and analyzes the technology stack of a software project.
 * 
 * This class is responsible for:
 * 1. Scanning project files to identify programming languages, frameworks, and tools
 * 2. Using pattern matching to detect common technologies
 * 3. Leveraging AI to analyze project structure and dependencies
 * 4. Caching results to improve performance
 * 
 * The detector uses a combination of static analysis and AI-powered
 * analysis to provide accurate stack detection.
 */
public class StackDetector {
    private static final Logger logger = LoggerFactory.getLogger(StackDetector.class);
    
    // Cache for storing detected stacks to avoid redundant analysis
    private static final Map<String, Stack> stackCache = new ConcurrentHashMap<>();
    
    // LLM client for AI-powered analysis
    private final LLMClient llmClient;
    
    // Stack detection results
    private String framework;
    private List<String> databases;
    private int runtimePort;
    
    /**
     * Creates a new StackDetector with default LLM client.
     */
    public StackDetector() {
        this(LLMClientFactory.createClient());
    }
    
    /**
     * Creates a new StackDetector with specified LLM client.
     * 
     * @param llmClient The LLM client to use for AI analysis
     */
    public StackDetector(LLMClient llmClient) {
        this.llmClient = llmClient;
        this.databases = new ArrayList<>();
        this.framework = "unknown";
        this.runtimePort = 8080;
    }

    // Pattern maps for detecting various technologies
    private static final Map<String, String> LANGUAGE_PATTERNS = Map.ofEntries(
        entry("java", ".*\\.java$"),
        entry("python", ".*\\.py$"),
        entry("javascript", ".*\\.js$|package\\.json$"),
        entry("typescript", ".*\\.ts$"),
        entry("go", ".*\\.go$"),
        entry("rust", ".*\\.rs$"),
        entry("php", ".*\\.php$"),
        entry("ruby", ".*\\.rb$"),
        entry("kotlin", ".*\\.kt$"),
        entry("scala", ".*\\.scala$"),
        entry("swift", ".*\\.swift$"),
        entry("csharp", ".*\\.cs$")
    );

    private static final Map<String, String> BUILD_TOOL_PATTERNS = Map.ofEntries(
        entry("maven", "pom\\.xml$"),
        entry("gradle", "build\\.gradle(\\.kts)?$"),
        entry("npm", "package\\.json$"),
        entry("yarn", "yarn\\.lock$"),
        entry("cargo", "Cargo\\.toml$"),
        entry("pip", "requirements\\.txt$|setup\\.py$|pyproject\\.toml$"),
        entry("composer", "composer\\.json$"),
        entry("bundler", "Gemfile$"),
        entry("sbt", "build\\.sbt$"),
        entry("dotnet", "\\.csproj$|\\.sln$")
    );

    private static final Map<String, String> FRAMEWORK_PATTERNS = Map.ofEntries(
        entry("spring", "org\\.springframework|@SpringBootApplication"),
        entry("django", "django|DJANGO_SETTINGS_MODULE"),
        entry("flask", "flask|Flask\\(|from flask"),
        entry("nodejs", "express|Express\\(|require\\('express'\\)|import express"),
        entry("rails", "Rails|rails"),
        entry("laravel", "laravel|Laravel"),
        entry("actix", "actix_web|actix-web|HttpServer|use actix"),
        entry("gin", "gin|gin\\.Default\\(\\)"),
        entry("nextjs", "next|Next|getStaticProps"),
        entry("nestjs", "@nestjs|@Injectable"),
        entry("fastapi", "fastapi|FastAPI"),
        entry("rocket", "rocket|Rocket::build")
    );

    private static final Map<String, String> DATABASE_PATTERNS = Map.ofEntries(
        entry("postgresql", "postgresql|postgres|psycopg2|pg|POSTGRES|DB_HOST|DATABASE_URL"),
        entry("mysql", "mysql|mariadb|MySQL|MYSQL"),
        entry("mongodb", "mongodb|mongo|Mongo|MONGO"),
        entry("redis", "redis|Redis|REDIS"),
        entry("elasticsearch", "elasticsearch|elastic"),
        entry("cassandra", "cassandra|Cassandra"),
        entry("dynamodb", "dynamodb|DynamoDB"),
        entry("sqlalchemy", "sqlalchemy|SQLAlchemy"),
        entry("cockroachdb", "cockroach|CockroachDB"),
        entry("neo4j", "neo4j|Neo4j")
    );

    private static final Map<String, String> CLOUD_PATTERNS = Map.of(
        "aws", "aws|AWS|amazon|s3|dynamodb|lambda",
        "gcp", "google|gcp|GCP|firebase|datastore",
        "azure", "azure|Azure|microsoft",
        "digitalocean", "digitalocean|DigitalOcean",
        "heroku", "heroku|Heroku"
    );

    private static final Map<String, String> TESTING_FRAMEWORK_PATTERNS = Map.of(
        "junit", "junit|@Test|Assert",
        "pytest", "pytest|@pytest",
        "jest", "jest|test\\(",
        "mocha", "mocha|describe\\(",
        "rspec", "rspec|describe",
        "phpunit", "phpunit|@test",
        "xunit", "xunit|[Test]",
        "testng", "testng|@Test"
    );

    private static final Map<String, Integer> DEFAULT_DB_PORTS = Map.ofEntries(
        entry("postgresql", 5432),
        entry("redis", 6379),
        entry("mongodb", 27017),
        entry("mysql", 3306),
        entry("elasticsearch", 9200),
        entry("cassandra", 9042),
        entry("dynamodb", 8000),
        entry("sqlite", 0), // SQLite doesn't use network ports
        entry("cockroachdb", 26257),
        entry("neo4j", 7687)
    );

    private static final Map<String, Integer> DEFAULT_APP_PORTS = Map.ofEntries(
        entry("flask", 5000),
        entry("express", 3000),
        entry("spring", 8080),
        entry("django", 8000),
        entry("rails", 3000),
        entry("laravel", 8000),
        entry("actix", 8080),
        entry("gin", 8080),
        entry("nextjs", 3000),
        entry("nestjs", 3000),
        entry("fastapi", 8000),
        entry("rocket", 8000)
    );

    /**
     * Detects the complete technology stack of a project.
     * 
     * @param projectDir The root directory of the project to analyze
     * @return A Stack object containing all detected technologies
     * @throws IOException if there are file system access issues
     */
    public Stack detectStack(Path projectDir) throws IOException {
        if (!Files.isDirectory(projectDir)) {
            throw new IllegalArgumentException("Invalid project directory: " + projectDir);
        }

        try {
            List<Path> files = listFiles(projectDir);
            if (files.isEmpty()) {
                throw new IllegalArgumentException("Empty project directory: " + projectDir);
            }

            String language = detectLanguage(files);
            String framework = detectFramework(files);
            String buildTool = detectBuildTool(files);
            List<String> databases = detectDatabases(files);
            Map<String, Integer> ports = detectPorts(files);
            List<String> cloudProviders = detectCloudProviders(files);
            List<String> testingFrameworks = detectTestingFrameworks(files);

            return new Stack(language, framework, buildTool, databases, ports, cloudProviders, testingFrameworks);
        } catch (IOException e) {
            throw new RuntimeException("Failed to detect stack", e);
        }
    }

    /**
     * Lists all files in a directory recursively.
     * 
     * @param dir The directory to scan
     * @return List of file paths
     * @throws IOException if there are file system access issues
     */
    private List<Path> listFiles(Path dir) throws IOException {
        try (Stream<Path> stream = Files.walk(dir)) {
            return stream
                .filter(Files::isRegularFile)
                .collect(Collectors.toList());
        }
    }

    /**
     * Detects the primary programming language used in the project.
     * Uses file extensions and content analysis.
     * 
     * @param files List of project files
     * @return The detected programming language
     */
    private String detectLanguage(List<Path> files) {
        for (Path file : files) {
            String fileName = file.getFileName().toString();
            for (Map.Entry<String, String> entry : LANGUAGE_PATTERNS.entrySet()) {
                if (fileName.matches(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        return "unknown";
    }

    /**
     * Detects the web framework used in the project.
     * Analyzes project structure and configuration files.
     * 
     * @param files List of project files
     * @return The detected framework name
     */
    private String detectFramework(List<Path> files) {
        for (Path file : files) {
            try {
                String content = Files.readString(file);
                for (Map.Entry<String, String> entry : FRAMEWORK_PATTERNS.entrySet()) {
                    if (content.matches(".*" + entry.getValue() + ".*")) {
                        return entry.getKey();
                    }
                }
            } catch (IOException e) {
                logger.warn("Failed to read file: {}", file, e);
            }
        }
        return "unknown";
    }

    /**
     * Detects build tools used in the project.
     * Looks for common build configuration files like pom.xml, build.gradle, etc.
     * 
     * @param files List of project files
     * @return The detected build tool name
     */
    private String detectBuildTool(List<Path> files) {
        for (Path file : files) {
            String fileName = file.getFileName().toString();
            for (Map.Entry<String, String> entry : BUILD_TOOL_PATTERNS.entrySet()) {
                if (fileName.matches(entry.getValue())) {
                    return entry.getKey();
                }
            }
        }
        return "unknown";
    }

    /**
     * Detects databases referenced in the project.
     * Analyzes configuration files and code for database connection strings and dependencies.
     * 
     * @param files List of project files
     * @return List of detected database names
     */
    private List<String> detectDatabases(List<Path> files) {
        Set<String> detectedDatabases = new HashSet<>();
        for (Path file : files) {
            try {
                String content = Files.readString(file);
                for (Map.Entry<String, String> entry : DATABASE_PATTERNS.entrySet()) {
                    if (content.matches(".*" + entry.getValue() + ".*")) {
                        detectedDatabases.add(entry.getKey());
                    }
                }
            } catch (IOException e) {
                logger.warn("Failed to read file: {}", file, e);
            }
        }
        return new ArrayList<>(detectedDatabases);
    }

    /**
     * Detects ports used in the project configuration.
     * Analyzes configuration files for port definitions and defaults.
     * 
     * @param files List of project files
     * @return Map of service names to their detected ports
     */
    private Map<String, Integer> detectPorts(List<Path> files) {
        Map<String, Integer> ports = new HashMap<>();
        String framework = detectFramework(files);
        List<String> databases = detectDatabases(files);

        // Add framework port
        DEFAULT_APP_PORTS.entrySet().stream()
            .filter(e -> e.getKey().equals(framework))
            .findFirst()
            .ifPresent(e -> ports.put("app", e.getValue()));

        // Add database ports
        databases.forEach(db -> 
            DEFAULT_DB_PORTS.entrySet().stream()
                .filter(e -> e.getKey().equals(db))
                .findFirst()
                .ifPresent(e -> ports.put(db, e.getValue()))
        );

        return ports;
    }

    /**
     * Detects cloud providers referenced in the project.
     * Analyzes configuration files and code for cloud service integrations.
     * 
     * @param files List of project files
     * @return List of detected cloud provider names
     */
    private List<String> detectCloudProviders(List<Path> files) {
        Set<String> providers = new HashSet<>();
        for (Path file : files) {
            try {
                String content = Files.readString(file);
                for (Map.Entry<String, String> entry : CLOUD_PATTERNS.entrySet()) {
                    if (content.matches(".*" + entry.getValue() + ".*")) {
                        providers.add(entry.getKey());
                    }
                }
            } catch (IOException e) {
                logger.warn("Failed to read file: {}", file, e);
            }
        }
        return new ArrayList<>(providers);
    }

    /**
     * Detects testing frameworks used in the project.
     * Analyzes test files and build configurations for testing dependencies.
     * 
     * @param files List of project files
     * @return List of detected testing framework names
     */
    private List<String> detectTestingFrameworks(List<Path> files) {
        Set<String> frameworks = new HashSet<>();
        for (Path file : files) {
            try {
                String content = Files.readString(file);
                for (Map.Entry<String, String> entry : TESTING_FRAMEWORK_PATTERNS.entrySet()) {
                    if (content.matches(".*" + entry.getValue() + ".*")) {
                        frameworks.add(entry.getKey());
                    }
                }
            } catch (IOException e) {
                logger.warn("Failed to read file: {}", file, e);
            }
        }
        return new ArrayList<>(frameworks);
    }

    /**
     * Uses AI to analyze project files for additional insights.
     * Sends project structure and key files to LLM for analysis.
     * 
     * @param files Set of files to analyze
     * @param language Detected programming language
     * @param projectDir Project root directory
     */
    private void analyzeWithAI(Set<Path> files, String language, Path projectDir) {
        try {
            // Build project context for AI
            StringBuilder context = new StringBuilder();
            context.append("Analyze this ").append(language).append(" project:\n\n");

            // Add main application files
            files.stream()
                .filter(this::isMainApplicationFile)
                .forEach(file -> {
                    try {
                        context.append("File: ").append(projectDir.relativize(file)).append("\n");
                        context.append(Files.readString(file)).append("\n\n");
                    } catch (IOException e) {
                        logger.warn("Failed to read file: {}", file, e);
                    }
                });

            // Add dependency files
            files.stream()
                .filter(this::isDependencyFile)
                .forEach(file -> {
                    try {
                        context.append("File: ").append(projectDir.relativize(file)).append("\n");
                        context.append(Files.readString(file)).append("\n\n");
                    } catch (IOException e) {
                        logger.warn("Failed to read file: {}", file, e);
                    }
                });

            String prompt = context.toString() + "\nAnalyze the code and identify:\n" +
                "1. Framework used\n" +
                "2. Database technologies used\n" +
                "3. Runtime port\n" +
                "4. API endpoints\n" +
                "5. Environment variables\n" +
                "Provide the response in a structured format.";

            String analysis = llmClient.generateText(prompt);
            logger.info("AI Analysis result: {}", analysis);
            updateStackFromAIAnalysis(analysis);
        } catch (Exception e) {
            logger.error("Failed to perform AI analysis", e);
        }
    }

    /**
     * Determines if a file is likely the main application file.
     * 
     * @param file The file to check
     * @return true if the file appears to be the main application file
     */
    private boolean isMainApplicationFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return fileName.equals("app.py") ||
               fileName.equals("main.py") ||
               fileName.equals("application.py") ||
               fileName.equals("server.py") ||
               fileName.equals("index.js") ||
               fileName.equals("app.js") ||
               fileName.equals("main.rs") ||
               fileName.equals("main.go") ||
               fileName.startsWith("application") && fileName.endsWith(".java");
    }

    /**
     * Determines if a file is a dependency configuration file.
     * 
     * @param file The file to check
     * @return true if the file is a dependency configuration file
     */
    private boolean isDependencyFile(Path file) {
        String fileName = file.getFileName().toString().toLowerCase();
        return fileName.equals("requirements.txt") ||
               fileName.equals("package.json") ||
               fileName.equals("cargo.toml") ||
               fileName.equals("go.mod") ||
               fileName.equals("build.gradle") ||
               fileName.equals("pom.xml");
    }

    /**
     * Updates stack information based on AI analysis results.
     * 
     * @param analysis The AI analysis response
     */
    private void updateStackFromAIAnalysis(String analysis) {
        try {
            // Extract sections from the AI's response
            Map<String, String> sections = parseAIResponse(analysis);
            
            // Update framework if it was unknown
            String detectedFramework = sections.get("framework");
            if (detectedFramework != null && !"unknown".equals(detectedFramework)) {
                this.framework = detectedFramework.toLowerCase();
            }
            
            // Update databases
            String dbSection = sections.get("databases");
            if (dbSection != null) {
                Arrays.stream(dbSection.split(","))
                    .map(String::trim)
                    .map(String::toLowerCase)
                    .filter(db -> !this.databases.contains(db))
                    .forEach(this.databases::add);
            }
            
            // Update runtime port if it was default
            String portStr = sections.get("port");
            if (portStr != null && this.runtimePort == 8080) {
                try {
                    int port = Integer.parseInt(portStr.trim());
                    if (port > 0 && port < 65536) {
                        this.runtimePort = port;
                    }
                } catch (NumberFormatException e) {
                    logger.warn("Failed to parse port number from AI analysis: {}", portStr);
                }
            }
            
            logger.info("Updated stack information from AI analysis: framework={}, databases={}, port={}",
                this.framework, this.databases, this.runtimePort);
        } catch (Exception e) {
            logger.error("Failed to update stack from AI analysis", e);
        }
    }

    /**
     * Parses the AI response into a structured format.
     * 
     * @param response The raw AI response
     * @return Map of detected technologies and their details
     */
    private Map<String, String> parseAIResponse(String response) {
        Map<String, String> sections = new HashMap<>();
        String[] lines = response.split("\n");
        String currentSection = null;
        StringBuilder currentContent = new StringBuilder();

        for (String line : lines) {
            line = line.trim();
            if (line.isEmpty()) continue;

            if (line.startsWith("Framework:")) {
                if (currentSection != null) {
                    sections.put(currentSection, currentContent.toString().trim());
                }
                currentSection = "framework";
                currentContent = new StringBuilder(line.substring("Framework:".length()).trim());
            } else if (line.startsWith("Database:") || line.startsWith("Databases:")) {
                if (currentSection != null) {
                    sections.put(currentSection, currentContent.toString().trim());
                }
                currentSection = "databases";
                currentContent = new StringBuilder(line.substring(line.indexOf(":") + 1).trim());
            } else if (line.startsWith("Port:") || line.startsWith("Runtime Port:")) {
                if (currentSection != null) {
                    sections.put(currentSection, currentContent.toString().trim());
                }
                currentSection = "port";
                currentContent = new StringBuilder(line.substring(line.indexOf(":") + 1).trim());
            } else if (line.startsWith("Endpoints:") || line.startsWith("API Endpoints:")) {
                if (currentSection != null) {
                    sections.put(currentSection, currentContent.toString().trim());
                }
                currentSection = "endpoints";
                currentContent = new StringBuilder(line.substring(line.indexOf(":") + 1).trim());
            } else if (line.startsWith("Environment:") || line.startsWith("Environment Variables:")) {
                if (currentSection != null) {
                    sections.put(currentSection, currentContent.toString().trim());
                }
                currentSection = "environment";
                currentContent = new StringBuilder(line.substring(line.indexOf(":") + 1).trim());
            } else if (currentSection != null) {
                currentContent.append("\n").append(line);
            }
        }

        if (currentSection != null) {
            sections.put(currentSection, currentContent.toString().trim());
        }

        return sections;
    }
} 