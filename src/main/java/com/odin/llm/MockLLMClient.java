package com.odin.llm;

import java.io.IOException;
import java.util.Map;

/**
 * Mock LLM client for testing purposes.
 * This client returns predefined responses instead of making actual API calls.
 */
public class MockLLMClient implements LLMClient {
    
    @Override
    public String generateText(String prompt) throws IOException {
        return generateText(prompt, Map.of());
    }
    
    @Override
    public String generateText(String prompt, Map<String, Object> parameters) {
        // Return predefined responses based on the prompt and type
        String type = parameters.getOrDefault("type", "").toString();
        
        if (type.equals("docker-compose") || prompt.contains("docker-compose")) {
            return generateDockerComposeResponse(prompt);
        } else if (type.equals("dockerfile") || prompt.contains("Dockerfile")) {
            return generateDockerfileResponse(prompt);
        } else if (type.equals("terraform") || prompt.contains("terraform")) {
            return generateTerraformResponse(prompt);
        } else if (type.equals("github-actions") || prompt.contains("GitHub Actions") || prompt.contains("workflow")) {
            return generateGitHubActionsResponse(prompt);
        } else {
            return "This is a mock response for testing purposes.";
        }
    }
    
    private String generateDockerComposeResponse(String prompt) {
        if (prompt.contains("mongodb")) {
            return "version: '3'\nservices:\n  app:\n    build: .\n  mongodb:\n    image: mongo:latest";
        } else if (prompt.contains("postgres")) {
            return "version: '3'\nservices:\n  app:\n    build: .\n  postgres:\n    image: postgres:latest";
        } else if (prompt.contains("redis")) {
            return "version: '3'\nservices:\n  app:\n    build: .\n  redis:\n    image: redis:latest";
        } else if (prompt.contains("networks")) {
            return "version: '3'\nservices:\n  app:\n    networks:\n      - backend\nnetworks:\n  backend:";
        } else if (prompt.contains("volumes")) {
            return "version: '3'\nservices:\n  app:\n    volumes:\n      - data:/data\nvolumes:\n  data:";
        } else {
            return "version: '3'\nservices:\n  app:\n    build: .";
        }
    }
    
    private String generateDockerfileResponse(String prompt) {
        if (prompt.contains("java") || prompt.contains("Java")) {
            return "FROM openjdk:17-slim\nWORKDIR /app\nCOPY target/*.jar app.jar\nCMD [\"java\", \"-jar\", \"app.jar\"]";
        } else if (prompt.contains("node") || prompt.contains("Node.js")) {
            return "FROM node:16\nWORKDIR /app\nCOPY package*.json ./\nRUN npm install\nCOPY . .\nCMD [\"npm\", \"start\"]";
        } else if (prompt.contains("python") || prompt.contains("Python")) {
            return "FROM python:3.9-slim\nWORKDIR /app\nCOPY requirements.txt .\nRUN pip install -r requirements.txt\nCOPY . .\nCMD [\"python\", \"app.py\"]";
        } else if (prompt.contains("multi-stage")) {
            return "FROM node:16 AS builder\nWORKDIR /app\nCOPY . .\nRUN npm install && npm run build\n\nFROM nginx:alpine\nCOPY --from=builder /app/dist /usr/share/nginx/html";
        } else if (prompt.contains("database")) {
            return "FROM python:3.9-slim\nENV DATABASE_URL=postgresql://user:pass@db:5432/app\nWORKDIR /app\nCOPY . .\nCMD [\"python\", \"app.py\"]";
        } else if (prompt.contains("environment")) {
            return "FROM node:16\nENV NODE_ENV=production\nENV PORT=3000\nWORKDIR /app\nCOPY . .\nCMD [\"npm\", \"start\"]";
        } else {
            return "FROM node:16\nWORKDIR /app\nCOPY . .\nCMD [\"npm\", \"start\"]";
        }
    }
    
    private String generateTerraformResponse(String prompt) {
        if (prompt.contains("aws")) {
            return "provider \"aws\" {\n  region = \"us-west-2\"\n}\n\nresource \"aws_instance\" \"app\" {\n  ami = \"ami-123\"\n  instance_type = \"t2.micro\"\n}";
        } else if (prompt.contains("gcp")) {
            return "provider \"google\" {\n  project = \"my-project\"\n  region  = \"us-central1\"\n}\n\nresource \"google_compute_instance\" \"app\" {\n  name = \"app-instance\"\n  machine_type = \"e2-micro\"\n}";
        } else if (prompt.contains("database")) {
            return "resource \"aws_db_instance\" \"db\" {\n  engine = \"postgres\"\n  instance_class = \"db.t3.micro\"\n}";
        } else if (prompt.contains("variables")) {
            return "variable \"environment\" {}\n\nresource \"aws_instance\" \"app\" {\n  tags = {\n    Environment = var.environment\n  }\n}";
        } else if (prompt.contains("outputs")) {
            return "output \"instance_ip\" {\n  value = aws_instance.app.public_ip\n}";
        } else {
            return "provider \"aws\" {}\n\nresource \"aws_instance\" \"app\" {}";
        }
    }
    
    private String generateGitHubActionsResponse(String prompt) {
        if (prompt.contains("aws")) {
            return "name: AWS Deploy\non: [push]\njobs:\n  deploy:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: aws-actions/configure-aws-credentials@v1";
        } else if (prompt.contains("gcp")) {
            return "name: GCP Deploy\non: [push]\njobs:\n  deploy:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: google-github-actions/auth@v1";
        } else if (prompt.contains("security")) {
            return "name: Security Scan\non: [push]\njobs:\n  scan:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v3\n      - uses: snyk/actions/node@master";
        } else if (prompt.contains("test")) {
            return "name: Tests\non: [push]\njobs:\n  test:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v3\n      - run: npm test";
        } else {
            return "name: CI\non: [push]\njobs:\n  build:\n    runs-on: ubuntu-latest\n    steps:\n      - uses: actions/checkout@v3";
        }
    }
    
    @Override
    public String generateInfrastructureCode(String prompt, String type) {
        return generateText(prompt, Map.of("type", type));
    }
} 