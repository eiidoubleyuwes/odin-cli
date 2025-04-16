package com.odin.generators;

import com.odin.detection.Stack;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.io.IOException;
import java.util.Arrays;
import static org.junit.jupiter.api.Assertions.*;

public class DockerfileGeneratorTest {
    @TempDir
    Path tempDir;
    
    private DockerfileGenerator generator;
    private Stack stack;
    
    @BeforeEach
    void setUp() {
        generator = new DockerfileGenerator("ollama");
        stack = new Stack();
    }
    
    @Test
    void testGenerateNodeJsDockerfile() throws IOException {
        stack.setLanguage("javascript");
        stack.setFramework("node");
        
        Path outputPath = tempDir.resolve("Dockerfile");
        generator.generateDockerfile(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("FROM node:"));
        assertTrue(content.contains("WORKDIR /app"));
        assertTrue(content.contains("COPY package*.json"));
        assertTrue(content.contains("RUN npm install"));
    }
    
    @Test
    void testGeneratePythonDockerfile() throws IOException {
        stack.setLanguage("python");
        stack.setFramework("flask");
        
        Path outputPath = tempDir.resolve("Dockerfile");
        generator.generateDockerfile(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("FROM python:"));
        assertTrue(content.contains("WORKDIR /app"));
        assertTrue(content.contains("COPY requirements.txt"));
        assertTrue(content.contains("RUN pip install"));
    }
    
    @Test
    void testGenerateJavaDockerfile() throws IOException {
        stack.setLanguage("java");
        stack.setFramework("spring");
        
        Path outputPath = tempDir.resolve("Dockerfile");
        generator.generateDockerfile(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("FROM maven:"));
        assertTrue(content.contains("WORKDIR /app"));
        assertTrue(content.contains("COPY pom.xml"));
        assertTrue(content.contains("RUN mvn package"));
    }
    
    @Test
    void testGenerateDockerfileWithDatabase() throws IOException {
        stack.setLanguage("python");
        stack.setFramework("django");
        stack.setDatabases(Arrays.asList("postgresql"));
        
        Path outputPath = tempDir.resolve("Dockerfile");
        generator.generateDockerfile(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("psycopg2"));
    }
    
    @Test
    void testGenerateMultiStageDockerfile() throws IOException {
        stack.setLanguage("java");
        stack.setFramework("spring");
        
        Path outputPath = tempDir.resolve("Dockerfile");
        generator.generateDockerfile(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("FROM maven:"));
        assertTrue(content.contains("FROM openjdk:"));
    }
    
    @Test
    void testGenerateDockerfileWithEnvironmentVariables() throws IOException {
        stack.setLanguage("node");
        stack.setFramework("express");
        
        Path outputPath = tempDir.resolve("Dockerfile");
        generator.generateDockerfile(stack, outputPath);
        
        assertTrue(outputPath.toFile().exists());
        String content = java.nio.file.Files.readString(outputPath);
        assertTrue(content.contains("ENV NODE_ENV=production"));
    }
} 