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
        if (prompt.contains("docker-compose")) {
            return "version: '3'\n\nservices:\n  app:\n    build: .\n    ports:\n      - \"5000:5000\"\n    environment:\n      DATABASE_URL: postgres://user:password@postgres:5432/database\n    depends_on:\n      - postgres\n    restart: always\n\n  postgres:\n    image: postgres\n    environment:\n      POSTGRES_USER: user\n      POSTGRES_PASSWORD: password\n      POSTGRES_DB: database\n    ports:\n      - \"5432:5432\"\n    volumes:\n      - ./postgres-data:/var/lib/postgresql/data\n    healthcheck:\n      test: [\"CMD\", \"pg_isready\", \"-U\", \"user\"]\n      interval: 10s\n      retries: 5";
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