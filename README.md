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

- **`core-agent`**: Contains core interfaces and data models (`Agent`, `Conversation`, `Message`, `Sender`)
- **`java-agent`**: Plain Java implementation with ReAct-style reasoning, memory, and tool execution capabilities
- **`springai-agent`**: Spring AI-based implementation with built-in LLM integration and function calling
- **`embabel-agent`**: Embabel platform integration for advanced agent capabilities
- **`web-app`**: Spring Boot web application with Thymeleaf templates providing the user interface

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
# Build all modules
./mvnw clean compile

# Run tests for all modules
./mvnw test

# Package the application
./mvnw clean package

# Install all modules (recommended for first setup)
./mvnw clean install
```

### Running the Application

#### Default Configuration (Spring AI)

```bash
./mvnw spring-boot:run -pl web-app
```

The application will start on `http://localhost:8080` using the Spring AI agent implementation.

If you see the app and get back a dummy response, you are ready to go.