package com.odin.cli;

import com.odin.detection.StackDetector;
import com.odin.llm.LLMClientFactory;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import static org.junit.jupiter.api.Assertions.*;
import org.junit.jupiter.api.condition.DisabledIfEnvironmentVariable;

public class OdinCommandTest {
    @TempDir
    Path tempDir;
    
    private OdinCommand odinCommand;
    
    @BeforeEach
    void setUp() throws Exception {
        // Set test mode to use MockLLMClient
        System.setProperty("ODIN_TEST_MODE", "true");
        
        // Create a test project directory
        Path projectDir = tempDir.resolve("test-project");
        Files.createDirectories(projectDir);
        
        // Create a simple test file
        Files.writeString(projectDir.resolve("app.py"), "print('Hello, World!')");
        
        // Initialize the command
        odinCommand = new OdinCommand();
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testInitCommand() throws Exception {
        String[] args = {"init", tempDir.toString(), "--provider", "ollama"};
        int exitCode = odinCommand.call();
        
        assertEquals(0, exitCode);
        assertTrue(Files.exists(tempDir.resolve(".odin")));
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testDockerCommand() throws Exception {
        String[] args = {"docker", tempDir.toString(), "--output", tempDir.resolve("output").toString(), "--provider", "ollama"};
        int exitCode = odinCommand.call();
        
        assertEquals(0, exitCode);
        assertTrue(Files.exists(tempDir.resolve("output/Dockerfile")));
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testComposeCommand() throws Exception {
        String[] args = {"compose", tempDir.resolve("test-project").toString(), "--provider", "ollama", "--output", tempDir.resolve("docker").toString()};
        int exitCode = odinCommand.call();
        
        assertEquals(0, exitCode);
        assertTrue(Files.exists(tempDir.resolve("docker/docker-compose.yml")));
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testTerraformCommand() throws Exception {
        String[] args = {"terraform", tempDir.resolve("test-project").toString(), "--provider", "ollama", "--output", tempDir.resolve("terraform").toString()};
        int exitCode = odinCommand.call();
        
        assertEquals(0, exitCode);
        assertTrue(Files.exists(tempDir.resolve("terraform/main.tf")));
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testActionsCommand() throws Exception {
        String[] args = {"actions", tempDir.toString(), "--output", tempDir.resolve("output").toString(), "--provider", "ollama", "--cloud", "aws"};
        int exitCode = odinCommand.call();
        
        assertEquals(0, exitCode);
        assertTrue(Files.exists(tempDir.resolve("output/.github/workflows")));
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testAllCommand() throws Exception {
        String[] args = {"all", tempDir.toString(), "--output", tempDir.resolve("output").toString(), "--provider", "ollama", "--cloud", "aws", "--concurrent", "--threads", "4"};
        int exitCode = odinCommand.call();
        
        assertEquals(0, exitCode);
        assertTrue(Files.exists(tempDir.resolve("output/Dockerfile")));
        assertTrue(Files.exists(tempDir.resolve("output/docker-compose.yml")));
        assertTrue(Files.exists(tempDir.resolve("output/terraform")));
        assertTrue(Files.exists(tempDir.resolve("output/.github/workflows")));
    }
    
    @Test
    @DisabledIfEnvironmentVariable(named = "CI", matches = "true")
    void testConfigCommand() throws Exception {
        String[] args = {"config", "--provider", "ollama", "--cloud", "aws", "--threads", "4", "--timeout", "300", "--output", tempDir.resolve("output").toString()};
        int exitCode = odinCommand.call();
        
        assertEquals(0, exitCode);
        assertTrue(Files.exists(Path.of(System.getProperty("user.home"), ".odin", "config.json")));
    }
    
    @Test
    void testDockerfileCommand() throws Exception {
        String[] args = {"dockerfile", tempDir.resolve("test-project").toString(), "--provider", "ollama", "--output", tempDir.resolve("docker").toString()};
        int exitCode = odinCommand.call();
        
        assertEquals(0, exitCode);
        assertTrue(Files.exists(tempDir.resolve("docker/Dockerfile")));
    }
    
    @Test
    void testGitHubActionsCommand() throws Exception {
        String[] args = {"github-actions", tempDir.resolve("test-project").toString(), "--provider", "ollama", "--output", tempDir.resolve(".github/workflows").toString()};
        int exitCode = odinCommand.call();
        
        assertEquals(0, exitCode);
        assertTrue(Files.exists(tempDir.resolve(".github/workflows/ci.yml")));
    }
    
    @Test
    void testInvalidCommand() throws Exception {
        String[] args = {"invalid-command", tempDir.resolve("test-project").toString()};
        int exitCode = odinCommand.call();
        
        assertNotEquals(0, exitCode);
    }
    
    @Test
    void testInvalidProjectDirectory() throws Exception {
        String[] args = {"compose", "/non/existent/path", "--provider", "ollama"};
        int exitCode = odinCommand.call();
        
        assertNotEquals(0, exitCode);
    }
} 