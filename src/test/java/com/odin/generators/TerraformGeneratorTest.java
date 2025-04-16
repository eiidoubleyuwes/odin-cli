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

public class TerraformGeneratorTest {
    @TempDir
    Path tempDir;
    
    private TerraformGenerator generator;
    private Stack stack;
    
    @BeforeEach
    void setUp() {
        stack = new Stack("", "", "", new ArrayList<>(), new HashMap<>(), new ArrayList<>(), new ArrayList<>());
    }
    
    @Test
    void testGenerateAWSTerraform() throws IOException {
        generator = new TerraformGenerator("ollama", "aws");
        stack.setLanguage("node");
        stack.setFramework("express");
        
        Path outputDir = tempDir.resolve("terraform");
        generator.generateTerraform(stack, outputDir);
        
        // Check main files
        assertTrue(outputDir.resolve("main.tf").toFile().exists());
        assertTrue(outputDir.resolve("variables.tf").toFile().exists());
        assertTrue(outputDir.resolve("outputs.tf").toFile().exists());
        assertTrue(outputDir.resolve("provider.tf").toFile().exists());
        
        // Check AWS-specific content
        String mainContent = java.nio.file.Files.readString(outputDir.resolve("main.tf"));
        assertTrue(mainContent.contains("aws_ecs_cluster"));
        assertTrue(mainContent.contains("aws_ecs_task_definition"));
        assertTrue(mainContent.contains("aws_ecs_service"));
    }
    
    @Test
    void testGenerateGCPTerraform() throws IOException {
        generator = new TerraformGenerator("ollama", "gcp");
        stack.setLanguage("python");
        stack.setFramework("flask");
        
        Path outputDir = tempDir.resolve("terraform");
        generator.generateTerraform(stack, outputDir);
        
        // Check main files
        assertTrue(outputDir.resolve("main.tf").toFile().exists());
        assertTrue(outputDir.resolve("variables.tf").toFile().exists());
        assertTrue(outputDir.resolve("outputs.tf").toFile().exists());
        assertTrue(outputDir.resolve("provider.tf").toFile().exists());
        
        // Check GCP-specific content
        String mainContent = java.nio.file.Files.readString(outputDir.resolve("main.tf"));
        assertTrue(mainContent.contains("google_cloud_run_service"));
        assertTrue(mainContent.contains("google_cloud_run_service_iam_member"));
    }
    
    @Test
    void testGenerateTerraformWithDatabase() throws IOException {
        generator = new TerraformGenerator("ollama", "aws");
        stack.setLanguage("node");
        stack.setFramework("express");
        stack.setDatabases(Arrays.asList("postgresql"));
        
        Path outputDir = tempDir.resolve("terraform");
        generator.generateTerraform(stack, outputDir);
        
        String mainContent = java.nio.file.Files.readString(outputDir.resolve("main.tf"));
        assertTrue(mainContent.contains("aws_db_instance"));
        assertTrue(mainContent.contains("aws_db_subnet_group"));
    }
    
    @Test
    void testGenerateTerraformWithVariables() throws IOException {
        generator = new TerraformGenerator("ollama", "aws");
        stack.setLanguage("python");
        stack.setFramework("django");
        
        Path outputDir = tempDir.resolve("terraform");
        generator.generateTerraform(stack, outputDir);
        
        String variablesContent = java.nio.file.Files.readString(outputDir.resolve("variables.tf"));
        assertTrue(variablesContent.contains("variable \"environment\""));
        assertTrue(variablesContent.contains("variable \"project_name\""));
        assertTrue(variablesContent.contains("variable \"region\""));
    }
    
    @Test
    void testGenerateTerraformWithOutputs() throws IOException {
        generator = new TerraformGenerator("ollama", "aws");
        stack.setLanguage("java");
        stack.setFramework("spring");
        
        Path outputDir = tempDir.resolve("terraform");
        generator.generateTerraform(stack, outputDir);
        
        String outputsContent = java.nio.file.Files.readString(outputDir.resolve("outputs.tf"));
        assertTrue(outputsContent.contains("output \"service_url\""));
        assertTrue(outputsContent.contains("output \"database_endpoint\""));
    }
    
    @Test
    void testGenerateTerraformWithProvider() throws IOException {
        generator = new TerraformGenerator("ollama", "aws");
        stack.setLanguage("go");
        stack.setFramework("gin");
        
        Path outputDir = tempDir.resolve("terraform");
        generator.generateTerraform(stack, outputDir);
        
        String providerContent = java.nio.file.Files.readString(outputDir.resolve("provider.tf"));
        assertTrue(providerContent.contains("provider \"aws\""));
        assertTrue(providerContent.contains("region"));
    }
} 