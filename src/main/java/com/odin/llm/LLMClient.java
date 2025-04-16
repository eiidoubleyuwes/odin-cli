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
    String generateText(String prompt, Map<String, Object> parameters);
    String generateInfrastructureCode(String prompt, String type);
}

/**
 * Factory for creating LLM client instances.
 * Supports multiple providers and handles client configuration.
 * Makes it easy to switch between different LLM backends.
 */
class LLMClientFactory {

}

/**
 * Implementation of LLMClient for Ollama.
 * Communicates with a local Ollama instance for text generation.
 * Handles API requests and response parsing.
 */
class OllamaClient implements LLMClient {

} 