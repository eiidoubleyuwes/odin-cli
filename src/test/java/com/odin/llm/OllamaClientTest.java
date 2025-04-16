package com.odin.llm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class OllamaClientTest {
    @Mock
    private HttpClient httpClient;

    private OllamaClient ollamaClient;

    @BeforeEach
    void setUp() {
        ollamaClient = new OllamaClient(httpClient);
    }

    @Test
    void testGenerateText() throws IOException, InterruptedException {
        // Mock HTTP response
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("""
            {
                "model": "codellama",
                "response": "Here's a sample Dockerfile:\\n\\nFROM openjdk:17\\nWORKDIR /app\\nCOPY . .\\nRUN ./mvnw package\\nEXPOSE 8080\\nCMD [\\"java\\", \\"-jar\\", \\"target/app.jar\\"]",
                "done": true
            }
            """);
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        // Test generateText method
        String prompt = "Generate a Dockerfile for a Java Spring Boot application";
        String response = ollamaClient.generateText(prompt);

        // Verify response
        assertNotNull(response);
        assertTrue(response.contains("FROM openjdk:17"));
        assertTrue(response.contains("WORKDIR /app"));
        assertTrue(response.contains("COPY . ."));
        assertTrue(response.contains("RUN ./mvnw package"));
        assertTrue(response.contains("EXPOSE 8080"));
        assertTrue(response.contains("CMD [\"java\", \"-jar\", \"target/app.jar\"]"));

        // Verify HTTP client was called
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGenerateTextWithError() throws IOException, InterruptedException {
        // Mock HTTP client to throw an exception
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenThrow(new IOException("Connection failed"));

        // Test generateText method with error
        String prompt = "Generate a Dockerfile";
        assertThrows(RuntimeException.class, () -> ollamaClient.generateText(prompt));

        // Verify HTTP client was called
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }

    @Test
    void testGenerateTextWithInvalidJson() throws IOException, InterruptedException {
        // Mock HTTP response with invalid JSON
        HttpResponse<String> mockResponse = mock(HttpResponse.class);
        when(mockResponse.body()).thenReturn("Invalid JSON response");
        when(httpClient.send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class)))
            .thenReturn(mockResponse);

        // Test generateText method with invalid JSON
        String prompt = "Generate a Dockerfile";
        assertThrows(RuntimeException.class, () -> ollamaClient.generateText(prompt));

        // Verify HTTP client was called
        verify(httpClient, times(1)).send(any(HttpRequest.class), any(HttpResponse.BodyHandler.class));
    }
} 