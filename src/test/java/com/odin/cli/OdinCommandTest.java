package com.odin.cli;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.io.IOException;
import static org.junit.jupiter.api.Assertions.*;

public class OdinCommandTest {
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        // Setup code if needed
    }
    
    @Test
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