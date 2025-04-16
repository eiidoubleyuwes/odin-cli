# Odin - AI-Powered DevOps Automation

Odin is an AI-powered DevOps automation tool written in Java that helps developers generate and manage infrastructure code using local LLM models through Ollama or cloud-based models like Gemini. It automatically analyzes your application stack and generates optimized infrastructure code, including Docker configurations, cloud infrastructure, and CI/CD pipelines.

## Features

### Stack Detection
- **Language Detection**: Automatically identifies Python, Node.js, Java, and Rust applications
- **Framework Analysis**: Detects frameworks like Spring Boot, Express.js, Django, Flask, and Actix
- **Dependency Analysis**: 
  - Package managers (npm, pip, maven, cargo)
  - Database dependencies (PostgreSQL, MongoDB, MySQL)
  - Cache systems (Redis, Memcached)
  - Message queues (RabbitMQ, Kafka)

### Infrastructure Generation
- **Dockerfile Generation**:
  - Multi-stage builds for optimized images
  - Development and production configurations
  - Security hardening
  - Layer optimization
- **Docker Compose**:
  - Service orchestration
  - Volume management
  - Network configuration
  - Environment variables
- **Terraform Configuration**:
  - AWS resources (EC2, ECS, RDS, S3)
  - GCP resources (GKE, Cloud SQL, Cloud Storage)
  - VPC and networking
  - Security groups and IAM
- **GitHub Actions**:
  - Build and test workflows
  - Deployment pipelines
  - Security scanning
  - Infrastructure validation

### Smart Analysis
- **Security Analysis**:
  - Dependency vulnerability scanning
  - Container security checks
  - Infrastructure security validation
  - Best practices enforcement
- **Cost Analysis**:
  - Resource cost estimation
  - Optimization recommendations
  - Scaling cost projections
- **Best Practices**:
  - Cloud provider guidelines
  - Security standards
  - Performance optimization
  - Maintainability checks

### Multiple LLM Support
- **Ollama Integration**:
  - Local model execution
  - Custom model support
  - Offline capabilities
- **Gemini Integration**:
  - Cloud-based processing
  - High-performance inference
  - Advanced context handling

## Prerequisites

### System Requirements
- **Java 21 or later**:
  - OpenJDK or Oracle JDK
  - Minimum 4GB RAM
  - 2 CPU cores
- **Maven 3.8 or later**:
  - For building and dependency management
  - Maven wrapper included
- **Docker and Docker Compose**:
  - Docker Engine 20.10+
  - Docker Compose v2+
  - Docker Desktop (recommended for GUI)

### LLM Requirements
- **Ollama (Local)**:
  - 8GB RAM minimum
  - 20GB disk space
  - GPU support (optional)
- **Gemini (Cloud)**:
  - Internet connection
  - API key
  - Rate limits consideration

### Cloud Provider Setup
- **AWS**:
  - AWS CLI configured
  - IAM user with appropriate permissions
  - Region configuration
- **GCP**:
  - Google Cloud SDK installed
  - Service account with required roles
  - Project configuration

## Project Structure

```
odin/
├── src/main/java/com/odin/
│   ├── cli/                    # Command-line interface
│   │   ├── commands/          # Command implementations
│   │   ├── options/           # Command options
│   │   └── utils/             # CLI utilities
│   ├── generators/            # Infrastructure generators
│   │   ├── docker/           # Docker-related generators
│   │   ├── terraform/        # Terraform generators
│   │   └── github/           # GitHub Actions generators
│   ├── detection/            # Stack detection
│   │   ├── analyzers/        # Language analyzers
│   │   └── parsers/          # Configuration parsers
│   ├── analysis/             # Analysis tools
│   │   ├── security/         # Security analysis
│   │   ├── cost/            # Cost analysis
│   │   └── bestpractices/   # Best practices validation
│   ├── llm/                 # LLM integration
│   │   ├── ollama/          # Ollama client
│   │   └── gemini/          # Gemini client
│   ├── explainer/           # Infrastructure explanation
│   ├── validators/          # Configuration validation
│   ├── model/              # Data models
│   └── Odin.java           # Main entry point
├── app_tests/              # Test applications
│   ├── python_app/         # Python test app
│   ├── node_app/          # Node.js test app
│   ├── java_spring_app/   # Java Spring test app
│   └── rust_app/          # Rust test app
├── pom.xml                # Maven configuration
└── .env                   # Environment variables
```

## Local Development Setup

### 1. Install Java 21
```bash
# Ubuntu/Debian
sudo apt update
sudo apt install openjdk-21-jdk

# macOS
brew install openjdk@21

# Verify installation
java -version
```

### 2. Install Maven
```bash
# Ubuntu/Debian
sudo apt install maven

# macOS
brew install maven

# Verify installation
mvn -version
```

### 3. Install Ollama
```bash
# Linux
curl -fsSL https://ollama.com/install.sh | sh

# macOS
brew install ollama

# Start Ollama service
ollama serve

# Pull required models
ollama pull codellama:13b
ollama pull mistral:7b
```

### 4. Install Docker
```bash
# Ubuntu/Debian
sudo apt install docker.io docker-compose

# macOS
brew install docker docker-compose

# Start Docker service
sudo systemctl start docker

# Verify installation
docker --version
docker-compose --version
```

### 5. Clone and Build
```bash
# Clone repository
git clone https://github.com/eiidoubleyuwes/odin-cli.git

# Build project
mvn clean package

# Verify build
java -jar target/odin-1.0-SNAPSHOT.jar --version
```

### 6. Set up Odin Alias
```bash
# For bash
echo 'alias odin="java -jar $(pwd)/target/odin-1.0-SNAPSHOT.jar"' >> ~/.bashrc
source ~/.bashrc

# For zsh
echo 'alias odin="java -jar $(pwd)/target/odin-1.0-SNAPSHOT.jar"' >> ~/.zshrc
source ~/.zshrc
```

### 7. Environment Configuration
Create `.env` file:
```env
# LLM Configuration
GEMINI_API_KEY=your_gemini_api_key
OLLAMA_HOST=localhost
OLLAMA_PORT=11434

# AWS Configuration
AWS_ACCESS_KEY_ID=your_aws_access_key
AWS_SECRET_ACCESS_KEY=your_aws_secret_key
AWS_REGION=your_aws_region

# GCP Configuration
GOOGLE_APPLICATION_CREDENTIALS=path/to/service-account.json
GCP_PROJECT_ID=your_project_id

# Application Configuration
LOG_LEVEL=INFO
CONCURRENT_GENERATION=true
MAX_THREADS=4
```

## Usage

### Basic Commands

#### 1. Generate Infrastructure
```bash
# Using Ollama provider
odin all /path/to/your/app \
  -o /path/to/output \
  --provider ollama \
  --cloud aws \
  --concurrent \
  --threads 4

# Using Gemini provider
odin all /path/to/your/app \
  -o /path/to/output \
  --provider gemini \
  --cloud aws \
  --verbose
```

#### 2. Individual Components
```bash
# Generate Dockerfile only
odin docker /path/to/app -o ./docker

# Generate Terraform only
odin terraform /path/to/app --cloud aws -o ./terraform

# Generate GitHub Actions
odin actions /path/to/app --cloud aws -o ./.github/workflows
```

#### 3. Analysis and Validation
```bash
# Security analysis
odin analyze security /path/to/app

# Cost estimation
odin analyze cost /path/to/app

# Best practices validation
odin validate /path/to/app
```

### Supported Application Types

#### Python Applications
- **Frameworks**:
  - Django
  - Flask
  - FastAPI
  - Pyramid
- **Package Managers**:
  - pip
  - poetry
  - pipenv

#### Node.js Applications
- **Frameworks**:
  - Express.js
  - NestJS
  - Next.js
  - Nuxt.js
- **Package Managers**:
  - npm
  - yarn
  - pnpm

#### Java Spring Applications
- **Spring Boot**
- **Spring Cloud**
- **Spring Data**
- **Build Tools**:
  - Maven
  - Gradle

#### Rust Applications
- **Frameworks**:
  - Actix
  - Rocket
  - Warp
- **Package Manager**:
  - Cargo

### Output Structure

#### Docker Configuration
```
docker/
├── Dockerfile              # Main container configuration
├── Dockerfile.dev         # Development configuration
├── docker-compose.yml     # Service orchestration
└── .dockerignore         # Docker ignore rules
```

#### Terraform Configuration
```
terraform/
├── main.tf               # Main infrastructure configuration
├── variables.tf          # Variable definitions
├── outputs.tf           # Output definitions
├── providers.tf         # Provider configuration
└── modules/            # Reusable modules
    ├── networking/     # Network configuration
    ├── compute/       # Compute resources
    └── database/      # Database resources
```

#### GitHub Actions
```
.github/workflows/
├── deploy.yml          # Deployment workflow
├── test.yml           # Testing workflow
└── security.yml       # Security scanning
```

## Component Details

### CLI (`cli/`)
- **Command Handling**:
  - Subcommand parsing
  - Option validation
  - Help generation
- **User Interaction**:
  - Progress indicators
  - Error handling
  - Interactive prompts
- **Configuration Management**:
  - Environment loading
  - Default settings
  - User preferences

### Generators (`generators/`)
- **DockerfileGenerator**:
  - Multi-stage builds
  - Layer optimization
  - Security hardening
- **TerraformGenerator**:
  - Resource creation
  - Module management
  - State handling
- **GitHubActionsGenerator**:
  - Workflow creation
  - Secret management
  - Environment setup

### Detection (`detection/`)
- **StackDetector**:
  - File pattern matching
  - Configuration parsing
  - Dependency analysis
- **DependencyAnalyzer**:
  - Version detection
  - Compatibility checking
  - Update recommendations

### Analysis (`analysis/`)
- **SecurityAnalyzer**:
  - Vulnerability scanning
  - Compliance checking
  - Security best practices
- **CostAnalyzer**:
  - Resource pricing
  - Usage estimation
  - Optimization suggestions
- **BestPracticesValidator**:
  - Cloud provider guidelines
  - Security standards
  - Performance benchmarks

### LLM Integration (`llm/`)
- **OllamaClient**:
  - Local model management
  - Prompt engineering
  - Response parsing
- **GeminiClient**:
  - API integration
  - Rate limiting
  - Error handling

### Explainer (`explainer/`)
- **Documentation Generation**:
  - Infrastructure diagrams
  - Usage guides
  - Best practices
- **Code Comments**:
  - Configuration explanation
  - Resource documentation
  - Usage examples

### Validators (`validators/`)
- **Configuration Validation**:
  - Syntax checking
  - Schema validation
  - Dependency verification
- **Compatibility Checking**:
  - Version compatibility
  - Resource limits
  - Service dependencies

## Contributing

### Development Workflow
1. Fork the repository
2. Create a feature branch
3. Implement changes
4. Add tests
5. Update documentation
6. Create pull request

### Code Style
- Follow Google Java Style Guide
- Use meaningful variable names
- Add comprehensive comments
- Write unit tests

### Testing
```bash
# Run all tests
mvn test

# Run specific test
mvn test -Dtest=TestClassName

# Run with coverage
mvn test jacoco:report
```

## License

This project is licensed under the MIT License - see the LICENSE file for details. 