package com.odin.llm;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LLMClientFactory {
    private static final Logger logger = LoggerFactory.getLogger(LLMClientFactory.class);

    public static LLMClient createClient() {
        return createClient(System.getenv("LLM_PROVIDER"));
    }

    public static LLMClient createClient(String provider) {
        // Check if we're in test mode
        if (System.getProperty("ODIN_TEST_MODE") != null) {
            logger.info("Test mode detected, using MockLLMClient");
            return new MockLLMClient();
        }
        
        if (provider == null || provider.isEmpty()) {
            provider = "ollama";
        }

        logger.info("Creating LLM client for provider: {}", provider);
        return switch (provider.toLowerCase()) {
            case "ollama" -> new OllamaClient();
            case "gemini" -> new GeminiClient();
            default -> {
                logger.error("Unknown LLM provider: {}", provider);
                throw new IllegalArgumentException("Unsupported LLM provider: " + provider);
            }
        };
    }
} 