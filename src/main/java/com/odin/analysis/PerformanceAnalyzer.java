package com.odin.analysis;

import com.odin.detection.Stack;
import com.odin.llm.LLMClient;
import com.odin.llm.LLMClientFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Map;
import java.util.stream.Stream;

public class PerformanceAnalyzer {
    private static final Logger logger = LoggerFactory.getLogger(PerformanceAnalyzer.class);
    private final LLMClient llmClient;

    private static final Map<String, Object> MCP_PARAMS = Map.of(
        "temperature", 0.7,
        "top_p", 0.9,
        "frequency_penalty", 0.5,
        "presence_penalty", 0.5,
        "max_tokens", 2000
    );

    public PerformanceAnalyzer(String provider) {
        this.llmClient = LLMClientFactory.createClient(provider);
    }
    
    public PerformanceAnalyzer(LLMClient llmClient) {
        this.llmClient = llmClient;
    }

    public void analyzePerformance(Stack stack, Path outputDir) throws IOException {
        logger.info("Analyzing performance for {} {} application", stack.getLanguage(), stack.getFramework());
        
        // Create output directory if it doesn't exist
        Files.createDirectories(outputDir);
        
        // Generate performance analysis report
        Path reportPath = outputDir.resolve("performance-analysis.md");
        generatePerformanceReport(stack, reportPath);
        
        logger.info("Generated performance analysis report at: {}", reportPath);
    }
    
    private void generatePerformanceReport(Stack stack, Path outputPath) throws IOException {
        StringBuilder prompt = new StringBuilder();
        prompt.append("Generate a performance analysis report for a ").append(stack.getLanguage())
              .append(" application using ").append(stack.getFramework())
              .append(" framework.\n");
        
        prompt.append("The report should include:\n");
        prompt.append("- Performance characteristics of the framework\n");
        prompt.append("- Potential bottlenecks\n");
        prompt.append("- Scaling considerations\n");
        prompt.append("- Resource requirements\n");
        prompt.append("- Optimization recommendations\n");
        prompt.append("Format the output as a Markdown document.\n");

        String reportContent = llmClient.generateText(prompt.toString(), MCP_PARAMS);
        Files.writeString(outputPath, reportContent);
        logger.info("Generated performance analysis report at: {}", outputPath);
    }

    private String collectCodeSamples(Path projectDir) throws IOException {
        // Collect relevant code files based on file extensions
        try (Stream<Path> paths = Files.walk(projectDir)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String fileName = path.toString().toLowerCase();
                    return fileName.endsWith(".java") || 
                           fileName.endsWith(".py") || 
                           fileName.endsWith(".js") || 
                           fileName.endsWith(".go") ||
                           fileName.endsWith(".rs");
                })
                .limit(10) // Limit to 10 files for analysis
                .map(path -> {
                    try {
                        return "File: " + path + "\n" + Files.readString(path);
                    } catch (IOException e) {
                        logger.warn("Failed to read file: {}", path, e);
                        return "";
                    }
                })
                .reduce("", String::concat);
        }
    }

    private String analyzeDatabaseQueries(Path projectDir) throws IOException {
        // Look for database-related files and queries
        try (Stream<Path> paths = Files.walk(projectDir)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String fileName = path.toString().toLowerCase();
                    return fileName.contains("repository") || 
                           fileName.contains("dao") || 
                           fileName.contains("model") ||
                           fileName.contains("entity");
                })
                .limit(5)
                .map(path -> {
                    try {
                        return "Database File: " + path + "\n" + Files.readString(path);
                    } catch (IOException e) {
                        logger.warn("Failed to read database file: {}", path, e);
                        return "";
                    }
                })
                .reduce("", String::concat);
        }
    }

    private String analyzeAPIEndpoints(Path projectDir) throws IOException {
        // Look for API-related files
        try (Stream<Path> paths = Files.walk(projectDir)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String fileName = path.toString().toLowerCase();
                    return fileName.contains("controller") || 
                           fileName.contains("route") || 
                           fileName.contains("api") ||
                           fileName.contains("endpoint");
                })
                .limit(5)
                .map(path -> {
                    try {
                        return "API File: " + path + "\n" + Files.readString(path);
                    } catch (IOException e) {
                        logger.warn("Failed to read API file: {}", path, e);
                        return "";
                    }
                })
                .reduce("", String::concat);
        }
    }

    private String analyzeResourceUsage(Path projectDir) throws IOException {
        // Look for configuration and resource-related files
        try (Stream<Path> paths = Files.walk(projectDir)) {
            return paths
                .filter(Files::isRegularFile)
                .filter(path -> {
                    String fileName = path.toString().toLowerCase();
                    return fileName.contains("config") || 
                           fileName.contains("properties") || 
                           fileName.contains("yml") ||
                           fileName.contains("yaml");
                })
                .limit(5)
                .map(path -> {
                    try {
                        return "Resource File: " + path + "\n" + Files.readString(path);
                    } catch (IOException e) {
                        logger.warn("Failed to read resource file: {}", path, e);
                        return "";
                    }
                })
                .reduce("", String::concat);
        }
    }
} 