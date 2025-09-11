# Favourites MCP Server Remote

This application provides two interfaces for managing your favourite talks:
1. **MCP Server**: Exposes tools through Server-Sent Events (SSE) for programmatic access
2. **Web Application**: A user-friendly Bootstrap-based web interface for manual management

## Features

- Add favourite talks with title and speakers
- List favourite talks with optional speaker filtering
- Persistent storage in JSON format
- Remote accessibility through SSE (MCP Server)
- Web interface for easy manual management

## Web Application

The web application provides a Bootstrap-styled interface for managing favourites:

- **Home Page** (`/`): Overview and navigation
- **View Favourites** (`/favourites`): List all favourites with filtering options
- **Add Favourite** (`/favourites/add`): Form to add new favourite talks

### Web Interface Features
- User-friendly forms with validation
- Filter favourites by speaker name
- **Editable User ID**: Change the user ID directly in forms and navigation
- Support for multiple users with easy switching
- Navigation bar with user ID input for quick user switching
- Responsive Bootstrap design
- Success/error message notifications

## MCP Server Tools

### add-favourite
Add a favourite talk for a user.

**Parameters:**
- `userId` (string): The user ID to add the favourite for
- `favouriteTalk` (FavouriteTalk): Object containing title and speakers array

**Example:**
```json
{
  "userId": "user123",
  "favouriteTalk": {
    "title": "Spring Boot Best Practices",
    "speakers": ["John Doe", "Jane Smith"]
  }
}
```

### list-favourites
List all favourite talks for a user, with optional filtering by speaker name.

**Parameters:**
- `userId` (string): The user ID to list favourites for
- `speakerNameFilter` (string, optional): Filter results by speaker name

**Example:**
```json
{
  "userId": "user123",
  "speakerNameFilter": "John"
}
```

## Running the Application

```bash
mvn spring-boot:run
```

The application will start on port 8081 with:
- Web interface available at `http://localhost:8081/`
- MCP tools exposed through SSE at `/mcp`

## Usage Examples

### Web Interface
1. Open `http://localhost:8081/` in your browser
2. **Change User**: Enter a different user ID in the navigation bar or in any form
3. Click "Add Favourite" to add a new talk (you can specify the user ID in the form)
4. Use "View Favourites" to see all your saved talks
5. Filter by speaker name using the search form
6. Switch between users easily using the User ID input in the navigation bar

### MCP Client
Connect your MCP client to the SSE endpoint at `http://localhost:8081/mcp` to access the programmatic tools.

## Storage

Favourites are stored in `data/favourites.json` with the following format:

```json
{
  "user123": [
    {
      "title": "Spring Boot Best Practices",
      "speakers": ["John Doe", "Jane Smith"]
    }
  ]
}
```

## Project Structure

- `src/main/java/org/rag4j/agent/mcp/`
  - `App.java` - Main Spring Boot application
  - `FavouritesService.java` - Business logic with MCP tool annotations
  - `FavouritesRepository.java` - Data persistence layer
  - `controller/` - Web controllers for HTTP endpoints
  - `model/` - Domain models and DTOs
- `src/main/resources/templates/` - Thymeleaf templates for web UI
- `src/test/` - Unit tests for all components

## Explore using MCP Inspector
You can explore the MCP server using the MCP Inspector tool:

```bash
npx @modelcontextprotocol/inspector
```