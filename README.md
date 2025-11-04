# JFall 2025 assignments
Workshop project for JFall 2025

From Scratch to Scalable: Building Smarter AI Agents with Frameworks

Jettro Coenradie
Daniël Spee

This is the repository for the workshop that Daniel and Jettro are presenting. If you join the workshop, you can prepare yourself by executing `step_0_setup.md` in the assignments folder.

The project for the workshop is a multi-module Spring Boot application that demonstrates various AI agent implementations with a web interface. The project showcases multiple approaches to building conversational agents, ranging from straightforward Java implementations that utilise OpenAI directly to Spring AI-based implementations and Embabel agent platform integration.

## Project Overview

The project focuses on conference talk management as a domain example, allowing users to interact with agents to search and retrieve information about conference talks and speakers through a web-based chat interface. A sidestep from this domain is the SciFi domain. It will become clear during the workshop.

Through spring profiles, you can choose the specific implementation. For `step_0`, the project contains a dummy `Agent` implementation, which is provided solely for you to start the application and verify its functionality.

## Architecture

### Module Structure

The project follows a clean architecture pattern with multiple agent implementations:

#### Core Modules
- **`core-agent`**: Contains core interfaces and data models (`Agent`, `Conversation`, `Message`, `Sender`)
- **`web-app`**: Spring Boot web application with Thymeleaf templates providing the main chat user interface

#### Agent Implementations
- **`java-agent`**: Plain Java implementation with ReAct-style reasoning, memory, and tool execution capabilities
- **`springai-agent`**: Spring AI-based implementation with built-in LLM integration and function calling
- **`embabel-agent`**: Embabel platform integration for advanced agent capabilities

#### Additional Modules
- **`auth-server`**: OAuth2/OIDC authentication server for securing agent endpoints
- **`evals-web-app`**: Evaluation and testing interface for assessing agent performance
- **`favourites-mcp`**: Model Context Protocol (MCP) server for managing favourite talks (stdio implementation)
- **`favourites-mcp-remote`**: Remote MCP server with web UI and OAuth2 security for managing favourites

### Key Features

- **Multiple Agent Implementations**: Switch between different agent backends without changing the web interface
- **Spring Profiles**: Easy configuration switching between agent implementations
- **Tool System**: Function calling capabilities for searching conference talks
- **Memory Management**: Conversation history and context management
- **Web Interface**: Clean, responsive chat interface using Thymeleaf templates
- **Multi-Agent Support**: Router agents that can delegate to specialised agents

## Getting Started

### Prerequisites

- Java 21 or later
- Maven 3.6 or later
- OpenAI API access (Optional, a proxy with OpenAI access is available)

### Building the Project

```bash
# Install all modules (recommended for first setup)
./mvnw clean install

# Skip tests during build (faster for initial setup)
./mvnw clean install -DskipTests

# Build all modules
./mvnw clean compile

# Run tests for all modules
./mvnw test

# Run tests for a specific module
./mvnw test -pl favourites-mcp-remote

# Package the application
./mvnw clean package
```

### Running the Application

#### Default Configuration (Dummy Agent)

```bash
./mvnw spring-boot:run -pl web-app
```

The application will start on `http://localhost:8080` using the default dummy agent implementation.

If you see the app and get back a dummy response, you are ready to go.

#### Running with Specific Agent Implementations

```bash
# Run with Plain Java agent
./mvnw spring-boot:run -pl web-app -Dspring-boot.run.profiles=plain

# Run with Spring AI agent
./mvnw spring-boot:run -pl web-app -Dspring-boot.run.profiles=springai

# Run with Embabel agent
./mvnw spring-boot:run -pl web-app -Dspring-boot.run.profiles=embabel

# Run on different port
./mvnw spring-boot:run -pl web-app -Dspring-boot.run.arguments=--server.port=8081
```

#### Running Other Applications

```bash
# Run the OAuth2 authentication server
./mvnw spring-boot:run -pl auth-server

# Run the evaluations web app
./mvnw spring-boot:run -pl evals-web-app

# Run the remote MCP server (with web UI)
./mvnw spring-boot:run -pl favourites-mcp-remote
```

## Testing

The project includes comprehensive tests for all modules:

```bash
# Run all tests
./mvnw test

# Run tests for specific module
./mvnw test -pl core-agent
./mvnw test -pl java-agent
./mvnw test -pl springai-agent
./mvnw test -pl web-app
./mvnw test -pl favourites-mcp-remote
```

**Note**: Tests use `@MockitoBean` for mocking dependencies. For controller tests with security, security auto-configuration is excluded to focus on testing controller logic.

## Workshop Assignments

The workshop is structured in progressive steps, each building on the previous:

- **Step 0**: Setup and verify the environment
- **Step 1**: Build a plain Java agent with ReAct pattern
- **Step 2**: Implement using Spring AI framework
- **Step 3**: Add Model Context Protocol (MCP) integration
- **Step 4**: Implement guardrails for safety
- **Step 5**: Add evaluation and testing
- **Step 6**: Integrate with Embabel platform
- **Step 7**: Add OAuth2 security

See the `assignments/` folder for detailed instructions on each step.
