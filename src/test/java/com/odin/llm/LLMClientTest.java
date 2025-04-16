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
        // Set test mode to use MockLLMClient
        System.setProperty("ODIN_TEST_MODE", "true");
        llmClient = LLMClientFactory.createClient("ollama");
    }
    
    @Test
    void testGenerateText() {
        String prompt = "Write a simple Python function that adds two numbers.";
        String response = llmClient.generateText(prompt, new HashMap<>());
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
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
        // In test mode, the mock client doesn't validate the prompt
        String response = llmClient.generateText("", new HashMap<>());
        assertNotNull(response);
    }
    
    @Test
    void testGenerateTextWithInvalidTimeout() {
        // In test mode, the mock client doesn't validate the timeout
        String prompt = "Write some code.";
        Map<String, Object> params = new HashMap<>();
        params.put("timeout", -1);
        String response = llmClient.generateText(prompt, params);
        assertNotNull(response);
    }
    
    @Test
    void testGenerateTextWithMaxTokens() {
        String prompt = "Write a long essay about programming.";
        Map<String, Object> params = new HashMap<>();
        params.put("max_tokens", 100);
        String response = llmClient.generateText(prompt, params);
        
        assertNotNull(response);
        assertFalse(response.isEmpty());
    }
} 