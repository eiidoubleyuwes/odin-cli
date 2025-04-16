package com.odin.explainer;

import com.odin.llm.OllamaClient;
import com.odin.llm.LLMClientFactory;
import com.odin.llm.LLMClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

public class Explainer {
    private static final Logger logger = LoggerFactory.getLogger(Explainer.class);
    private final LLMClient llmClient;

    public Explainer() {
        this.llmClient = LLMClientFactory.createClient();
    }
    
    public Explainer(String provider) {
        this.llmClient = LLMClientFactory.createClient(provider);
    }

    public Map<String, String> explainInfrastructure(Path directory) throws IOException {
        logger.info("Explaining infrastructure code in: {}", directory);
        Map<String, String> explanations = new HashMap<>();

        // Explain Dockerfile
        Path dockerfile = directory.resolve("Dockerfile");
        if (Files.exists(dockerfile)) {
            explanations.put("Dockerfile", explainFile(dockerfile, "Dockerfile"));
        }

        // Explain docker-compose.yml
        Path composeFile = directory.resolve("docker-compose.yml");
        if (Files.exists(composeFile)) {
            explanations.put("docker-compose.yml", explainFile(composeFile, "docker-compose.yml"));
        }

        // Explain Terraform files
        Path terraformDir = directory.resolve("terraform");
        if (Files.exists(terraformDir)) {
            explanations.put("main.tf", explainFile(terraformDir.resolve("main.tf"), "main.tf"));
            explanations.put("variables.tf", explainFile(terraformDir.resolve("variables.tf"), "variables.tf"));
            explanations.put("terraform.tfvars", explainFile(terraformDir.resolve("terraform.tfvars"), "terraform.tfvars"));
            explanations.put("outputs.tf", explainFile(terraformDir.resolve("outputs.tf"), "outputs.tf"));
        }

        // Explain GitHub Actions workflow
        Path workflowFile = directory.resolve(".github/workflows/deploy.yml");
        if (Files.exists(workflowFile)) {
            explanations.put("deploy.yml", explainFile(workflowFile, "GitHub Actions workflow"));
        }

        return explanations;
    }

    private String explainFile(Path file, String fileType) throws IOException {
        logger.info("Explaining {}: {}", fileType, file);
        
        String content = Files.readString(file);
        return explainCode(content);
    }
    
    public String explainCode(String code) {
        String prompt = String.format("""
            Explain the following code in detail:
            
            %s
            """, code);
            
        return llmClient.generateText(prompt);
    }

    public String getInfrastructureOverview(Map<String, String> explanations) {
        StringBuilder overview = new StringBuilder("Infrastructure Overview\n\n");
        
        if (explanations.containsKey("Dockerfile")) {
            overview.append("Container Configuration:\n");
            overview.append("- ").append(explanations.get("Dockerfile")).append("\n\n");
        }
        
        if (explanations.containsKey("docker-compose.yml")) {
            overview.append("Local Development Environment:\n");
            overview.append("- ").append(explanations.get("docker-compose.yml")).append("\n\n");
        }
        
        if (explanations.containsKey("main.tf")) {
            overview.append("Cloud Infrastructure:\n");
            overview.append("- ").append(explanations.get("main.tf")).append("\n\n");
        }
        
        if (explanations.containsKey("deploy.yml")) {
            overview.append("Deployment Pipeline:\n");
            overview.append("- ").append(explanations.get("deploy.yml")).append("\n\n");
        }
        
        return overview.toString();
    }

    public String getSecurityAnalysis(Map<String, String> explanations) {
        StringBuilder analysis = new StringBuilder("Security Analysis\n\n");
        
        // Analyze Dockerfile security
        if (explanations.containsKey("Dockerfile")) {
            analysis.append("Container Security:\n");
            String dockerfileExplanation = explanations.get("Dockerfile");
            if (dockerfileExplanation.contains("root")) {
                analysis.append("- Warning: Container running as root user\n");
            }
            if (dockerfileExplanation.contains("secrets")) {
                analysis.append("- Warning: Potential exposure of secrets in container\n");
            }
        }
        
        return analysis.toString();
    }

    public String getCostAnalysis(Map<String, String> explanations) {
        StringBuilder analysis = new StringBuilder("Cost Analysis\n\n");
        
        // Analyze ECS/Lambda/EC2 costs
        if (explanations.containsKey("main.tf")) {
            analysis.append("Cloud Resource Costs:\n");
            String terraformExplanation = explanations.get("main.tf");
            
            if (terraformExplanation.contains("FARGATE")) {
                analysis.append("- Using AWS Fargate for serverless container execution\n");
                analysis.append("- Estimated cost: $0.04048 per vCPU-hour, $0.004445 per GB-hour\n");
            } else if (terraformExplanation.contains("lambda")) {
                analysis.append("- Using AWS Lambda for serverless function execution\n");
                analysis.append("- Estimated cost: $0.0000166667 per GB-second\n");
            } else if (terraformExplanation.contains("instance_type")) {
                analysis.append("- Using EC2 instances for application hosting\n");
                analysis.append("- Estimated cost: Varies by instance type (t2.micro: ~$8.56/month)\n");
            }
            analysis.append("\n");
        }
        
        // Analyze database costs
        if (explanations.containsKey("docker-compose.yml")) {
            analysis.append("Database Costs:\n");
            String composeExplanation = explanations.get("docker-compose.yml");
            
            if (composeExplanation.contains("postgres")) {
                analysis.append("- PostgreSQL: Consider RDS for production (~$12.96/month for db.t3.micro)\n");
            }
            if (composeExplanation.contains("mysql")) {
                analysis.append("- MySQL: Consider RDS for production (~$12.96/month for db.t3.micro)\n");
            }
            if (composeExplanation.contains("mongodb")) {
                analysis.append("- MongoDB: Consider DocumentDB for production (~$12.96/month for db.t3.micro)\n");
            }
            if (composeExplanation.contains("redis")) {
                analysis.append("- Redis: Consider ElastiCache for production (~$12.96/month for cache.t3.micro)\n");
            }
            analysis.append("\n");
        }
        
        return analysis.toString();
    }
} 