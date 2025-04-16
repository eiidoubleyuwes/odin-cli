package com.odin.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import com.odin.detection.Stack;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The type Ollama client.
 */
public class OllamaClient implements LLMClient {
    private static final Logger logger = LoggerFactory.getLogger(OllamaClient.class);
    private static final String OLLAMA_API_URL = "http://localhost:11434/api/generate";
    private static final MediaType JSON = MediaType.get("application/json");
    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_SECONDS = 180;
    
    private final OkHttpClient client;
    private final ObjectMapper mapper;
    private final String model;

    /**
     * Instantiates a new Ollama client.
     */
    public OllamaClient() {
        this(System.getenv("OLLAMA_MODEL"));
    }

    /**
     * Instantiates a new Ollama client.
     *
     * @param model the model
     */
    public OllamaClient(String model) {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();
        this.mapper = new ObjectMapper();
        this.model = model != null ? model : "codellama";
    }

    /**
     * Instantiates a new Ollama client.
     *
     * @param httpClient the http client
     */
    public OllamaClient(HttpClient httpClient) {
        this.client = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();
        this.mapper = new ObjectMapper();
        this.model = System.getenv("OLLAMA_MODEL") != null ? System.getenv("OLLAMA_MODEL") : "codellama";
    }

    @Override
    public String generateText(String prompt) {
        return generateText(prompt, Map.of());
    }

    @Override
    public String generateText(String prompt, Map<String, Object> parameters) {
        int retries = 0;
        while (retries < MAX_RETRIES) {
            try {
                Map<String, Object> requestBody = Map.of(
                    "model", model,
                    "prompt", prompt,
                    "stream", false,
                    "options", parameters
                );

                String jsonBody = mapper.writeValueAsString(requestBody);
                logger.info("Sending prompt to LLM: {}", prompt);
                RequestBody body = RequestBody.create(jsonBody, JSON);
                Request request = new Request.Builder()
                    .url(OLLAMA_API_URL)
                    .post(body)
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected response code: " + response);
                    }

                    String responseBody = response.body().string();
                    logger.info("Received response from LLM: {}", responseBody);
                    Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
                    String result = (String) responseMap.get("response");
                    logger.info("Extracted response: {}", result);
                    return result;
                }
            } catch (Exception e) {
                retries++;
                if (retries >= MAX_RETRIES) {
                    logger.error("Failed to generate text after {} retries: {}", MAX_RETRIES, e.getMessage());
                    throw new RuntimeException("Failed to generate text", e);
                }
                logger.warn("Attempt {} failed, retrying in {} seconds...", retries, retries);
                try {
                    Thread.sleep(retries * 1000L);
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    throw new RuntimeException("Interrupted while waiting to retry", ie);
                }
            }
        }
        throw new RuntimeException("Failed to generate text after " + MAX_RETRIES + " retries");
    }

    @Override
    public String generateInfrastructureCode(String prompt, String type) {
        String enhancedPrompt = String.format("""
            You are an expert DevOps engineer. Generate infrastructure code for the following request:
            Type: %s
            
            Requirements:
            %s
            
            IMPORTANT: Return ONLY the raw code without any explanations, comments, markdown formatting, or code block markers.
            The code should be immediately executable without any modifications.
            """,
            type, prompt
        );
        
        String response = generateText(enhancedPrompt);
        // Strip out any markdown code block markers
        response = response.replaceAll("```[\\w]*\\n|```", "");
        
        // Remove any leading whitespace or empty lines
        response = response.replaceAll("^\\s+", "");
        
        // Remove any trailing whitespace or empty lines
        response = response.replaceAll("\\s+$", "");
        
        return response;
    }
} 