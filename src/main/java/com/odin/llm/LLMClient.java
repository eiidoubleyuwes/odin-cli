package com.odin.llm;

import java.io.IOException;
import java.util.Map;

/**
 * Interface for interacting with different LLM providers.
 * Allows easy switching between providers like Ollama, Gemini, etc.
 * Each provider implementation handles its own API communication and response parsing.
 */
public interface LLMClient {
    /**
     * Generates text based on the provided prompt.
     * Used for analyzing container logs and generating insights.
     * 
     * @param prompt The input text to send to the LLM
     * @return The generated response from the LLM
     * @throws IOException if there's a network or API error
     */
    String generateText(String prompt) throws IOException;
    
    /**
     * Generates text based on the provided prompt and parameters.
     * 
     * @param prompt The input text to send to the LLM
     * @param parameters Additional parameters for the LLM
     * @return The generated response from the LLM
     */
    String generateText(String prompt, Map<String, Object> parameters);
    
    /**
     * Generates infrastructure code based on the provided prompt.
     * 
     * @param prompt The input text describing the infrastructure requirements
     * @param type The type of infrastructure code to generate (e.g., "dockerfile", "docker-compose")
     * @return The generated infrastructure code
     */
    String generateInfrastructureCode(String prompt, String type);
}