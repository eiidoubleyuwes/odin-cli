package com.odin.llm;

import java.util.Map;

/**
 * Mock LLM client for testing purposes.
 * This client returns predefined responses instead of making actual API calls.
 */
public class MockLLMClient implements LLMClient {
    
    @Override
    public String generateText(String prompt) {
        return generateText(prompt, Map.of());
    }
    
    @Override
    public String generateText(String prompt, Map<String, Object> parameters) {
        // Return predefined responses based on the prompt
        if (prompt.contains("docker-compose.yml")) {
            if (prompt.contains("postgresql")) {
                return """
                version: '3'
                services:
                  app:
                    build: .
                    ports:
                      - "3000:3000"
                    networks:
                      - backend
                  postgres:
                    image: postgres:latest
                    environment:
                      - POSTGRES_DB=app
                      - POSTGRES_USER=user
                      - POSTGRES_PASSWORD=password
                    volumes:
                      - data:/var/lib/postgresql/data
                    networks:
                      - backend
                networks:
                  backend:
                volumes:
                  data:
                """;
            } else if (prompt.contains("mongodb")) {
                return """
                version: '3'
                services:
                  app:
                    build: .
                    ports:
                      - "3000:3000"
                    networks:
                      - backend
                  mongodb:
                    image: mongo:latest
                    environment:
                      - MONGO_INITDB_ROOT_USERNAME=user
                      - MONGO_INITDB_ROOT_PASSWORD=password
                    volumes:
                      - data:/data/db
                    networks:
                      - backend
                networks:
                  backend:
                volumes:
                  data:
                """;
            } else if (prompt.contains("redis")) {
                return """
                version: '3'
                services:
                  app:
                    build: .
                    ports:
                      - "3000:3000"
                    networks:
                      - backend
                  redis:
                    image: redis:latest
                    ports:
                      - "6379:6379"
                    networks:
                      - backend
                networks:
                  backend:
                """;
            } else {
                return """
                version: '3'
                services:
                  app:
                    build: .
                    ports:
                      - "3000:3000"
                    networks:
                      - backend
                networks:
                  backend:
                """;
            }
        } else if (prompt.contains("Dockerfile")) {
            return "FROM python:3.9-slim\n\nWORKDIR /app\n\nCOPY requirements.txt .\nRUN pip install --no-cache-dir -r requirements.txt\n\nCOPY . .\n\nEXPOSE 5000\n\nCMD [\"python\", \"app.py\"]";
        } else if (prompt.contains("terraform")) {
            return "terraform {\n  required_providers {\n    aws = {\n      source  = \"hashicorp/aws\"\n      version = \"~> 4.0\"\n    }\n  }\n}\n\nprovider \"aws\" {\n  region = \"us-west-2\"\n}\n\nresource \"aws_instance\" \"example\" {\n  ami           = \"ami-0c55b159cbfafe1f0\"\n  instance_type = \"t2.micro\"\n\n  tags = {\n    Name = \"example-instance\"\n  }\n}";
        } else if (prompt.contains("GitHub Actions")) {
            return "name: CI\n\non:\n  push:\n    branches: [ main ]\n  pull_request:\n    branches: [ main ]\n\njobs:\n  build:\n    runs-on: ubuntu-latest\n\n    steps:\n    - uses: actions/checkout@v3\n    - name: Set up JDK 17\n      uses: actions/setup-java@v3\n      with:\n        java-version: '17'\n        distribution: 'temurin'\n    - name: Build with Maven\n      run: mvn -B package --file pom.xml";
        } else {
            return "This is a mock response for testing purposes.";
        }
    }
    
    @Override
    public String generateInfrastructureCode(String prompt, String type) {
        return generateText(prompt, Map.of("type", type));
    }
} 