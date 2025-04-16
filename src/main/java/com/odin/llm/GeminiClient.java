package com.odin.llm;

import com.fasterxml.jackson.databind.ObjectMapper;
import okhttp3.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * The type Gemini client.
 */
public class GeminiClient implements LLMClient {
    private static final Logger logger = LoggerFactory.getLogger(GeminiClient.class);
    private static final String GEMINI_API_URL = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent";
    private static final MediaType JSON = MediaType.get("application/json");
    private static final int MAX_RETRIES = 3;
    private static final int TIMEOUT_SECONDS = 60;

    private final OkHttpClient client;
    private final ObjectMapper mapper;
    private final String apiKey;

    /**
     * Instantiates a new Gemini client.
     */
    public GeminiClient() {
        this(System.getenv("GEMINI_API_KEY"));
    }

    /**
     * Instantiates a new Gemini client.
     *
     * @param apiKey the api key
     */
    public GeminiClient(String apiKey) {
        if (apiKey == null || apiKey.isEmpty()) {
            throw new IllegalArgumentException("Gemini API key is required");
        }
        this.apiKey = apiKey;
        this.client = new OkHttpClient.Builder()
            .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
            .build();
        this.mapper = new ObjectMapper();
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
                Map<String, Object> promptContent = Map.of(
                    "parts", new Object[]{
                        Map.of("text", prompt)
                    }
                );

                Map<String, Object> requestBody = Map.of(
                    "contents", new Object[]{promptContent},
                    "generationConfig", Map.of(
                        "temperature", parameters.getOrDefault("temperature", 0.7),
                        "topP", parameters.getOrDefault("top_p", 0.8),
                        "topK", parameters.getOrDefault("top_k", 40),
                        "maxOutputTokens", parameters.getOrDefault("max_tokens", 2048)
                    )
                );

                logger.info("Sending prompt to Gemini: {}", prompt);
                String jsonBody = mapper.writeValueAsString(requestBody);
                RequestBody body = RequestBody.create(jsonBody, JSON);

                Request request = new Request.Builder()
                    .url(GEMINI_API_URL + "?key=" + apiKey)
                    .post(body)
                    .build();

                try (Response response = client.newCall(request).execute()) {
                    if (!response.isSuccessful()) {
                        throw new IOException("Unexpected response code: " + response);
                    }

                    String responseBody = response.body().string();
                    logger.info("Received response from Gemini: {}", responseBody);
                    Map<String, Object> responseMap = mapper.readValue(responseBody, Map.class);
                    
                    // Navigate through the Gemini response structure
                    var candidates = (java.util.List<?>) responseMap.get("candidates");
                    if (candidates == null || candidates.isEmpty()) {
                        throw new IOException("No candidates in response");
                    }
                    // Extract the first candidate from the list
                    var candidateContent = (Map<?, ?>) ((Map<?, ?>) candidates.get(0)).get("content");
                    var parts = (java.util.List<?>) candidateContent.get("parts");
                    // Get the first part of the response and extract the actual text field (i.e., the model's reply)
                    var text = (String) ((Map<?, ?>) parts.get(0)).get("text");
                    
                    logger.info("Extracted response from Gemini: {}", text);
                    return text;
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
            - Use the latest stable versions of all tools and dependencies
            - Follow security best practices
            - Include proper error handling and monitoring
            - Add comprehensive documentation
            - Ensure scalability and maintainability
            - Use cloud-native approaches where applicable
            
            Specific Requirements:
            %s
            
            Format the output as a valid configuration file without additional explanations.
            
            IMPORTANT: 
            1. Return ONLY the raw code without any explanations, comments, markdown formatting, or code block markers
            2. The code should be immediately executable without any modifications
            3. For Node.js applications:
               - Use Node.js LTS (not Alpine) as the base image
               - Use npm for package management
               - Do not mix with Python or other technologies
            4. For YAML files:
               - Do not include YAML front matter or markdown markers
               - Use proper indentation
            5. For Dockerfiles:
               - Use multi-stage builds where appropriate
               - Include proper security practices
               - Set up non-root users
               - Include healthchecks
            
            Return only the code without any explanations or comments.
            The code should be production-ready and follow all modern best practices.
            """,
            type, prompt
        );
        
        String response = generateText(enhancedPrompt);
        // Clean up the response by removing markdown code block markers and ensuring proper formatting
        return response.replaceAll("```(?:yaml|dockerfile)?\\s*", "")
                      .replaceAll("```\\s*$", "")
                      .trim();
    }
} 