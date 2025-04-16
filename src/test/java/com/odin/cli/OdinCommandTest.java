package com.odin.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

public class OdinCommandTest {
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Set test environment variable
        System.setProperty("ODIN_TEST_MODE", "true");
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testInitCommand() {
        String[] args = new String[] {
            "init",
            tempDir.toString(),
            "--provider", "ollama"
        };
        
        OdinCommand.main(args);
        assertTrue(tempDir.resolve(".odin").toFile().exists());
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testDockerCommand() {
        String[] args = new String[] {
            "docker",
            tempDir.toString(),
            "-o", tempDir.resolve("output").toString(),
            "--provider", "ollama"
        };
        
        OdinCommand.main(args);
        assertTrue(tempDir.resolve("output/Dockerfile").toFile().exists());
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testComposeCommand() {
        String[] args = new String[] {
            "compose",
            tempDir.toString(),
            "-o", tempDir.resolve("output").toString(),
            "--provider", "ollama"
        };
        
        OdinCommand.main(args);
        assertTrue(tempDir.resolve("output/docker-compose.yml").toFile().exists());
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testTerraformCommand() {
        String[] args = new String[] {
            "terraform",
            tempDir.toString(),
            "-o", tempDir.resolve("output").toString(),
            "--provider", "ollama",
            "--cloud", "aws"
        };
        
        OdinCommand.main(args);
        assertTrue(tempDir.resolve("output/terraform").toFile().exists());
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testActionsCommand() {
        String[] args = new String[] {
            "actions",
            tempDir.toString(),
            "-o", tempDir.resolve("output").toString(),
            "--provider", "ollama",
            "--cloud", "aws"
        };
        
        OdinCommand.main(args);
        assertTrue(tempDir.resolve("output/.github/workflows").toFile().exists());
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testAllCommand() {
        String[] args = new String[] {
            "all",
            tempDir.toString(),
            "-o", tempDir.resolve("output").toString(),
            "--provider", "ollama",
            "--cloud", "aws",
            "--concurrent",
            "--threads", "4"
        };
        
        OdinCommand.main(args);
        assertTrue(tempDir.resolve("output/Dockerfile").toFile().exists());
        assertTrue(tempDir.resolve("output/docker-compose.yml").toFile().exists());
        assertTrue(tempDir.resolve("output/terraform").toFile().exists());
        assertTrue(tempDir.resolve("output/.github/workflows").toFile().exists());
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testConfigCommand() {
        String[] args = new String[] {
            "config",
            "--provider", "ollama",
            "--cloud", "aws",
            "--threads", "4",
            "--timeout", "300",
            "-o", tempDir.resolve("output").toString()
        };
        
        OdinCommand.main(args);
        assertTrue(Path.of(System.getProperty("user.home"), ".odin", "config.json").toFile().exists());
    }
} 