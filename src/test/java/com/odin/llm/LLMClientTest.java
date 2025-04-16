package com.odin.llm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import java.util.HashMap;
import java.util.Map;
import static org.junit.jupiter.api.Assertions.*;

public class LLMClientTest {
    private LLMClient llmClient;
    
    @BeforeEach
    void setUp() {
        llmClient = LLMClientFactory.createClient("ollama");
    }
    
    @Test
    void testGenerateText() {
        String prompt = "Write a simple Python function that adds two numbers.";
        String response = llmClient.generateText(prompt, new HashMap<>());
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(response.contains("def") || response.contains("return"));
    }
    
    @Test
    void testGenerateTextWithContext() {
        String context = "We are building a web application.";
        String prompt = "Write a function to handle HTTP GET requests.";
        Map<String, Object> params = new HashMap<>();
        params.put("context", context);
        String response = llmClient.generateText(prompt, params);
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(response.contains("def") || response.contains("function"));
    }
    
    @Test
    void testGenerateTextWithTimeout() {
        String prompt = "Write a complex algorithm.";
        Map<String, Object> params = new HashMap<>();
        params.put("timeout", 30);
        String response = llmClient.generateText(prompt, params);
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
    
    @Test
    void testGenerateTextWithInvalidPrompt() {
        assertThrows(IllegalArgumentException.class, () -> {
            llmClient.generateText("", new HashMap<>());
        });
    }
    
    @Test
    void testGenerateTextWithInvalidTimeout() {
        String prompt = "Write some code.";
        Map<String, Object> params = new HashMap<>();
        params.put("timeout", -1);
        assertThrows(IllegalArgumentException.class, () -> {
            llmClient.generateText(prompt, params);
        });
    }
    
    @Test
    void testGenerateTextWithMaxTokens() {
        String prompt = "Write a long essay about programming.";
        Map<String, Object> params = new HashMap<>();
        params.put("max_tokens", 100);
        String response = llmClient.generateText(prompt, params);
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
        assertTrue(response.split("\\s+").length <= 100);
    }
} 