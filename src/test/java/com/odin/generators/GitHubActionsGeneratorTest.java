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

public class GitHubActionsGeneratorTest {
    @TempDir
    Path tempDir;
    
    private GitHubActionsGenerator generator;
    private Stack stack;
    
    @BeforeEach
    void setUp() {
        stack = new Stack("", "", "", new ArrayList<>(), new HashMap<>(), new ArrayList<>(), new ArrayList<>());
    }
    
    @Test
    void testGenerateAWSWorkflows() throws IOException {
        generator = new GitHubActionsGenerator("ollama", "aws");
        stack.setLanguage("node");
        stack.setFramework("express");
        
        Path outputDir = tempDir.resolve(".github/workflows");
        generator.generateWorkflows(stack, outputDir);
        
        // Check workflow files
        assertTrue(outputDir.resolve("ci.yml").toFile().exists());
        assertTrue(outputDir.resolve("cd.yml").toFile().exists());
        assertTrue(outputDir.resolve("test.yml").toFile().exists());
        assertTrue(outputDir.resolve("security.yml").toFile().exists());
        
        // Check AWS-specific content
        String cdContent = java.nio.file.Files.readString(outputDir.resolve("cd.yml"));
        assertTrue(cdContent.contains("aws-credentials"));
        assertTrue(cdContent.contains("aws ecs update-service"));
    }
    
    @Test
    void testGenerateGCPWorkflows() throws IOException {
        generator = new GitHubActionsGenerator("ollama", "gcp");
        stack.setLanguage("python");
        stack.setFramework("flask");
        
        Path outputDir = tempDir.resolve(".github/workflows");
        generator.generateWorkflows(stack, outputDir);
        
        // Check workflow files
        assertTrue(outputDir.resolve("ci.yml").toFile().exists());
        assertTrue(outputDir.resolve("cd.yml").toFile().exists());
        assertTrue(outputDir.resolve("test.yml").toFile().exists());
        assertTrue(outputDir.resolve("security.yml").toFile().exists());
        
        // Check GCP-specific content
        String cdContent = java.nio.file.Files.readString(outputDir.resolve("cd.yml"));
        assertTrue(cdContent.contains("google-github-actions/auth"));
        assertTrue(cdContent.contains("gcloud run deploy"));
    }
    
    @Test
    void testGenerateCIWorkflow() throws IOException {
        generator = new GitHubActionsGenerator("ollama", "aws");
        stack.setLanguage("java");
        stack.setFramework("spring");
        
        Path outputDir = tempDir.resolve(".github/workflows");
        generator.generateWorkflows(stack, outputDir);
        
        String ciContent = java.nio.file.Files.readString(outputDir.resolve("ci.yml"));
        assertTrue(ciContent.contains("on: [push, pull_request]"));
        assertTrue(ciContent.contains("jobs:"));
        assertTrue(ciContent.contains("build:"));
        assertTrue(ciContent.contains("test:"));
    }
    
    @Test
    void testGenerateTestWorkflow() throws IOException {
        generator = new GitHubActionsGenerator("ollama", "aws");
        stack.setLanguage("python");
        stack.setFramework("django");
        
        Path outputDir = tempDir.resolve(".github/workflows");
        generator.generateWorkflows(stack, outputDir);
        
        String testContent = java.nio.file.Files.readString(outputDir.resolve("test.yml"));
        assertTrue(testContent.contains("on: [push, pull_request]"));
        assertTrue(testContent.contains("jobs:"));
        assertTrue(testContent.contains("test:"));
        assertTrue(testContent.contains("python -m pytest"));
    }
    
    @Test
    void testGenerateSecurityWorkflow() throws IOException {
        generator = new GitHubActionsGenerator("ollama", "aws");
        stack.setLanguage("node");
        stack.setFramework("express");
        
        Path outputDir = tempDir.resolve(".github/workflows");
        generator.generateWorkflows(stack, outputDir);
        
        String securityContent = java.nio.file.Files.readString(outputDir.resolve("security.yml"));
        assertTrue(securityContent.contains("on: [schedule]"));
        assertTrue(securityContent.contains("jobs:"));
        assertTrue(securityContent.contains("security:"));
        assertTrue(securityContent.contains("snyk"));
    }
    
    @Test
    void testGenerateWorkflowsWithCustomTimeout() throws IOException {
        generator = new GitHubActionsGenerator("ollama", "aws");
        stack.setLanguage("go");
        stack.setFramework("gin");
        
        Path outputDir = tempDir.resolve(".github/workflows");
        generator.generateWorkflows(stack, outputDir);
        
        String cdContent = java.nio.file.Files.readString(outputDir.resolve("cd.yml"));
        assertTrue(cdContent.contains("timeout-minutes: 45"));
    }
} 