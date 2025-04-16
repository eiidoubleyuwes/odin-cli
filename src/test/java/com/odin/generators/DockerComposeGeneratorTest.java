package com.odin.generators;

import com.odin.detection.Stack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Arrays;
import java.util.ArrayList;
import java.util.HashMap;
import static org.junit.jupiter.api.Assertions.*;

public class DockerComposeGeneratorTest {
    @TempDir
    Path tempDir;
    
    private DockerComposeGenerator generator;
    private Stack stack;
    
    @BeforeEach
    void setUp() {
        generator = new DockerComposeGenerator("ollama");
        stack = new Stack("", "", "", new ArrayList<>(), new HashMap<>(), new ArrayList<>(), new ArrayList<>());
    }
    
    @Test
    void testGenerateBasicDockerCompose() throws IOException {
        stack.setLanguage("node");
        stack.setFramework("express");
        
        Path outputPath = tempDir.resolve("docker-compose.yml");
        generator.generateDockerCompose(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("version: '3'"));
        assertTrue(content.contains("services:"));
        assertTrue(content.contains("app:"));
    }
    
    @Test
    void testGenerateDockerComposeWithPostgres() throws IOException {
        stack.setLanguage("python");
        stack.setFramework("django");
        stack.setDatabases(Arrays.asList("postgresql"));
        
        Path outputPath = tempDir.resolve("docker-compose.yml");
        generator.generateDockerCompose(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("postgres:"));
        assertTrue(content.contains("POSTGRES_DB"));
        assertTrue(content.contains("POSTGRES_USER"));
        assertTrue(content.contains("POSTGRES_PASSWORD"));
    }
    
    @Test
    void testGenerateDockerComposeWithMongoDB() throws IOException {
        stack.setLanguage("node");
        stack.setFramework("express");
        stack.setDatabases(Arrays.asList("mongodb"));
        
        Path outputPath = tempDir.resolve("docker-compose.yml");
        generator.generateDockerCompose(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("mongodb:"));
        assertTrue(content.contains("MONGO_INITDB_ROOT_USERNAME"));
        assertTrue(content.contains("MONGO_INITDB_ROOT_PASSWORD"));
    }
    
    @Test
    void testGenerateDockerComposeWithRedis() throws IOException {
        stack.setLanguage("python");
        stack.setFramework("flask");
        stack.setDatabases(Arrays.asList("redis"));
        
        Path outputPath = tempDir.resolve("docker-compose.yml");
        generator.generateDockerCompose(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("redis:"));
        assertTrue(content.contains("REDIS_PASSWORD"));
    }
    
    @Test
    void testGenerateDockerComposeWithMultipleDatabases() throws IOException {
        stack.setLanguage("java");
        stack.setFramework("spring");
        stack.setDatabases(Arrays.asList("postgresql", "redis"));
        
        Path outputPath = tempDir.resolve("docker-compose.yml");
        generator.generateDockerCompose(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("postgres:"));
        assertTrue(content.contains("redis:"));
    }
    
    @Test
    void testGenerateDockerComposeWithNetworks() throws IOException {
        stack.setLanguage("node");
        stack.setFramework("express");
        stack.setDatabases(Arrays.asList("postgresql"));
        
        Path outputPath = tempDir.resolve("docker-compose.yml");
        generator.generateDockerCompose(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("networks:"));
        assertTrue(content.contains("app-network:"));
    }
    
    @Test
    void testGenerateDockerComposeWithVolumes() throws IOException {
        stack.setLanguage("python");
        stack.setFramework("django");
        stack.setDatabases(Arrays.asList("postgresql"));
        
        Path outputPath = tempDir.resolve("docker-compose.yml");
        generator.generateDockerCompose(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("volumes:"));
        assertTrue(content.contains("postgres_data:"));
    }
} 