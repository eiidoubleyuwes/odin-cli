package com.odin.detection;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import java.nio.file.Path;
import java.nio.file.Files;
import java.nio.file.Paths;
import static org.junit.jupiter.api.Assertions.*;

public class StackDetectorTest {
    private StackDetector detector;
    @TempDir
    Path tempDir;
    
    @BeforeEach
    void setUp() {
        detector = new StackDetector();
    }
    
    @Test
    void testDetectNodeJSStack() throws Exception {
        // Create package.json
        String packageJson = """
            {
                "name": "test-app",
                "version": "1.0.0",
                "dependencies": {
                    "express": "^4.17.1"
                }
            }
            """;
        Files.write(tempDir.resolve("package.json"), packageJson.getBytes());
        
        // Create app.js
        String appJs = """
            const express = require('express');
            const app = express();
            app.listen(3000);
            """;
        Files.write(tempDir.resolve("app.js"), appJs.getBytes());
        
        Stack stack = detector.detectStack(tempDir);
        
        assertNotNull(stack);
        assertEquals("javascript", stack.getLanguage());
        assertEquals("nodejs", stack.getFramework());
        assertTrue(stack.getDatabases().isEmpty());
    }
    
    @Test
    void testDetectPythonFlaskStack() throws Exception {
        // Create requirements.txt
        String requirements = """
            Flask==2.0.1
            SQLAlchemy==1.4.23
            """;
        Files.write(tempDir.resolve("requirements.txt"), requirements.getBytes());
        
        // Create app.py
        String appPy = """
            from flask import Flask
            app = Flask(__name__)
            
            @app.route('/')
            def hello():
                return 'Hello World'
            """;
        Files.write(tempDir.resolve("app.py"), appPy.getBytes());
        
        Stack stack = detector.detectStack(tempDir);
        
        assertNotNull(stack);
        assertEquals("python", stack.getLanguage());
        assertEquals("flask", stack.getFramework());
        assertTrue(stack.getDatabases().contains("sqlalchemy"));
    }
    
    @Test
    void testDetectJavaSpringStack() throws Exception {
        // Create pom.xml
        String pomXml = """
            <?xml version="1.0" encoding="UTF-8"?>
            <project xmlns="http://maven.apache.org/POM/4.0.0">
                <groupId>com.example</groupId>
                <artifactId>demo</artifactId>
                <version>0.0.1-SNAPSHOT</version>
                <dependencies>
                    <dependency>
                        <groupId>org.springframework.boot</groupId>
                        <artifactId>spring-boot-starter-web</artifactId>
                    </dependency>
                </dependencies>
            </project>
            """;
        Files.write(tempDir.resolve("pom.xml"), pomXml.getBytes());
        
        Stack stack = detector.detectStack(tempDir);
        
        assertNotNull(stack);
        assertEquals("java", stack.getLanguage());
        assertEquals("spring", stack.getFramework());
        assertTrue(stack.getDatabases().isEmpty());
    }
    
    @Test
    void testDetectGoStack() throws Exception {
        // Create go.mod
        String goMod = """
            module example.com/test
            
            go 1.16
            
            require (
                github.com/gin-gonic/gin v1.7.7
            )
            """;
        Files.write(tempDir.resolve("go.mod"), goMod.getBytes());
        
        // Create main.go
        String mainGo = """
            package main
            
            import "github.com/gin-gonic/gin"
            
            func main() {
                r := gin.Default()
                r.GET("/ping", func(c *gin.Context) {
                    c.JSON(200, gin.H{
                        "message": "pong",
                    })
                })
                r.Run()
            }
            """;
        Files.write(tempDir.resolve("main.go"), mainGo.getBytes());
        
        Stack stack = detector.detectStack(tempDir);
        
        assertNotNull(stack);
        assertEquals("go", stack.getLanguage());
        assertEquals("gin", stack.getFramework());
        assertTrue(stack.getDatabases().isEmpty());
    }
    
    @Test
    void testDetectEmptyDirectory() {
        assertThrows(IllegalArgumentException.class, () -> {
            detector.detectStack(tempDir);
        });
    }
    
    @Test
    void testDetectInvalidDirectory() {
        assertThrows(IllegalArgumentException.class, () -> {
            detector.detectStack(Paths.get("/invalid/path"));
        });
    }
} 