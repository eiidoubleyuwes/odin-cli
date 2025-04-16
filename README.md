# Odin - Infrastructure as Code Generator

Odin is a powerful CLI tool that automatically generates infrastructure code for your applications. It analyzes your project's technology stack and creates appropriate Docker, Docker Compose, Terraform, and GitHub Actions configurations.

## Features

### 1. Stack Detection
- **Language Detection**: Automatically identifies programming languages (Java, Python, Node.js, Go, etc.)
- **Framework Detection**: Recognizes web frameworks (Spring, Flask, Express, Gin, etc.)
- **Database Detection**: Identifies database technologies (PostgreSQL, MongoDB, Redis, etc.)
- **Build Tool Detection**: Detects build systems (Maven, Gradle, npm, etc.)
- **Cloud Provider Detection**: Identifies cloud service integrations (AWS, GCP, Azure)
- **Testing Framework Detection**: Recognizes testing frameworks (JUnit, pytest, Jest, etc.)

### 2. Docker Support
- **Dockerfile Generation**: Creates optimized Dockerfiles based on your application stack
- **Multi-stage Builds**: Generates efficient multi-stage Dockerfiles for smaller images
- **Environment Variables**: Automatically configures environment variables
- **Security Best Practices**: Implements security best practices in generated Dockerfiles
- **Docker Compose**: Generates complete docker-compose.yml files with:
  - Application service configuration
  - Database services
  - Network configuration
  - Volume management
  - Health checks

### 3. Infrastructure as Code
- **Terraform Generation**: Creates Terraform configurations for cloud providers
- **AWS Support**: Generates AWS-specific Terraform configurations
- **GCP Support**: Generates Google Cloud Platform Terraform configurations
- **Database Resources**: Includes database resources in Terraform configurations
- **Output Variables**: Configures output variables for resource information
- **Variable Definitions**: Includes variable definitions for customization

### 4. CI/CD Integration
- **GitHub Actions**: Generates GitHub Actions workflows
- **CI Workflows**: Creates continuous integration workflows
- **Security Scans**: Includes security scanning in workflows
- **Test Automation**: Configures test automation in CI pipelines
- **Custom Timeouts**: Supports custom timeout configurations
- **Cloud Provider Integration**: Includes cloud provider-specific workflows

### 5. Container Monitoring
- **Real-time Monitoring**: Monitors Docker containers in real-time
- **Resource Usage**: Tracks CPU, memory, and network usage
- **Log Analysis**: Analyzes container logs for issues
- **AI-Powered Insights**: Uses AI to provide insights on container health
- **Customizable Intervals**: Configurable monitoring intervals
- **Multiple LLM Providers**: Supports different LLM providers for analysis

## Project Structure

```
odin/
├── app_tests/                              # Test applications
│   ├── java_spring_app/                    # Spring Boot test app
│   ├── node_app/                          # Node.js test app
│   └── python_app/                        # Python test app
├── src/
│   ├── main/
│   │   └── java/
│   │       └── com/
│   │           └── odin/
│   │               ├── analysis/           # Performance analysis
│   │               │   └── PerformanceAnalyzer.java
│   │               ├── cli/               # Command-line interface
│   │               │   └── OdinCommand.java
│   │               ├── detection/         # Stack detection
│   │               │   ├── StackDetector.java
│   │               │   └── Stack.java
│   │               ├── explainer/         # Infrastructure explanation
│   │               │   └── Explainer.java
│   │               ├── generators/        # Code generators
│   │               │   ├── ConcurrentGenerator.java
│   │               │   ├── DockerComposeGenerator.java
│   │               │   ├── DockerfileGenerator.java
│   │               │   ├── GitHubActionsGenerator.java
│   │               │   └── TerraformGenerator.java
│   │               ├── llm/              # LLM integration
│   │               │   ├── GeminiClient.java
│   │               │   ├── LLMClientFactory.java
│   │               │   ├── LLMClient.java
│   │               │   ├── MockLLMClient.java
│   │               │   └── OllamaClient.java
│   │               ├── model/            # Data models
│   │               │   └── Stack.java
│   │               ├── monitoring/       # Container monitoring
│   │               │   └── DockerMonitor.java
│   │               ├── Odin.java         # Main entry point
│   │               └── validators/       # Infrastructure validation
│   │                   └── InfrastructureValidator.java
│   └── test/
│       └── java/
│           └── com/
│               └── odin/
│                   ├── analysis/         # Analysis tests
│                   ├── cli/             # CLI tests
│                   │   └── OdinCommandTest.java
│                   ├── detection/       # Detection tests
│                   │   └── StackDetectorTest.java
│                   ├── generators/      # Generator tests
│                   │   ├── DockerComposeGeneratorTest.java
│                   │   ├── DockerfileGeneratorTest.java
│                   │   ├── GitHubActionsGeneratorTest.java
│                   │   └── TerraformGeneratorTest.java
│                   ├── llm/            # LLM client tests
│                   │   ├── LLMClientTest.java
│                   │   ├── MockLLMClient.java
│                   │   └── OllamaClientTest.java
│                   └── validators/     # Validator tests
├── test_all_features.sh                 # Test script
├── test_project/                        # Test project
├── pom.xml                             # Maven configuration
└── README.md                           # Project documentation
```

## Commands

### Stack Detection
```bash
# Detect and summarize project stack using Ollama
java -jar target/odin-1.0-SNAPSHOT.jar init <project-directory> --provider ollama

# Detect and summarize project stack using Gemini
java -jar target/odin-1.0-SNAPSHOT.jar init <project-directory> --provider gemini
```

### Docker Generation
```bash
# Generate Dockerfile using Ollama
java -jar target/odin-1.0-SNAPSHOT.jar docker <project-directory> --output ./docker --provider ollama

# Generate Dockerfile using Gemini
java -jar target/odin-1.0-SNAPSHOT.jar docker <project-directory> --output ./docker --provider gemini

# Generate Docker Compose configuration
java -jar target/odin-1.0-SNAPSHOT.jar compose <project-directory> --output ./docker --provider ollama


```

### Infrastructure as Code
```bash
# Generate Terraform configuration for AWS
java -jar target/odin-1.0-SNAPSHOT.jar terraform <project-directory> --provider ollama --cloud aws --output ./terraform

# Generate Terraform configuration for GCP
java -jar target/odin-1.0-SNAPSHOT.jar terraform <project-directory> --provider ollama --cloud gcp --output ./terraform
```

### CI/CD
```bash
# Generate GitHub Actions workflows
java -jar target/odin-1.0-SNAPSHOT.jar actions <project-directory> --output <OutputDir> --provider ollama
```

### Container Monitoring
```bash
# Monitor Docker containers with Ollama
java -jar target/odin-1.0-SNAPSHOT.jar monitor --interval 60 

# Monitor Docker containers with Gemini
java -jar target/odin-1.0-SNAPSHOT.jar monitor --interval 60 
```

### All-in-One
```bash
# Generate all infrastructure files using Ollama
java -jar target/odin-1.0-SNAPSHOT.jar all <project-directory> --output ./infrastructure --provider ollama

# Generate all infrastructure files using Gemini
export GEMINI_API_KEY=AIzaSyBfghjkl;lkjhfdfghjkjhg
java -jar target/odin-1.0-SNAPSHOT.jar all <project-directory> --output ./infrastructure --provider gemini
```

## Requirements

- Java 17 or higher
- Maven 3.6 or higher
- Docker (for container monitoring)
- Ollama (for local LLM support)
  - Required models: codellama, llama3 ,however you can add your own
- Gemini API key (for cloud LLM support)
  - Set via environment variable: `GEMINI_API_KEY`

## Building

```bash
# Build the project
mvn clean package

# Build without running tests
mvn clean install -DskipTests
```

## Configuration

Odin can be configured using environment variables:

- `LLM_PROVIDER`: The LLM provider to use (default: "ollama")
- `LLM_MODEL`: The model to use (default: "codellama")
- `LLM_TIMEOUT`: Timeout in seconds for LLM requests (default: 30)
- `MONITOR_INTERVAL`: Interval in seconds for container monitoring (default: 30)
- `GEMINI_API_KEY`: API key for Gemini (required when using Gemini provider)

## License

This project is licensed under the MIT License - see the LICENSE file for details. 