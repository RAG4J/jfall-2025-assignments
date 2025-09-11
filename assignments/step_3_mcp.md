# Add an MCP server to manage favorite talks
In this assignment, you will add a new feature to the TalksAgent. You'll add an MCP server to manage the favorite talks of the users. The MCP server is already implemented for you in the favourites-mcp module. You only need to configure the MCP client.

This project builds on the previous assignment. Make sure you have completed the previous assignment before starting this one. Switch back to the spring profile springai before continuing.

```text
> The MCP server is in the favourites-mcp module. First, you need to build the module.
> From the root of the project, run `mvn clean install -DskipTests -pl favourites-mcp`
> Next you can test the MCP server with the MCP inspector. (Optional)
> You can skip this step if you do not have npx installed or don't want to install the inspector.
> In the file src/test/resources/mcp-servers-config.json, check the configuration and change the absolute path to the jar.
> From the root of the project, run:
npx @modelcontextprotocol/inspector --cli \
  --config ./src/test/resources/mcp-servers-config.json \
  --server location \
  --method tools/list
```

The output of the npx command should be something like this:

```json
{
  "tools": [
    {
      "name": "add-favourite",
      "description": "Add a favourite talk for a user.",
      "inputSchema": {
        "type": "object",
        "properties": {
          "userId": {
            "type": "string"
          },
          "favouriteTalk": {
            "type": "object",
            "properties": {
              "speakers": {
                "type": "array",
                "items": {
                  "type": "string"
                }
              },
              "title": {
                "type": "string"
              }
            },
            "required": [
              "speakers",
              "title"
            ]
          }
        },
        "required": [
          "userId",
          "favouriteTalk"
        ],
        "additionalProperties": false
      }
    },
    {
      "name": "list-favourites",
      "description": "List all favourite talks for a user, with optional filtering by speaker name.",
      "inputSchema": {
        "type": "object",
        "properties": {
          "userId": {
            "type": "string"
          },
          "speakerNameFilter": {
            "type": "string"
          }
        },
        "required": [
          "userId",
          "speakerNameFilter"
        ],
        "additionalProperties": false
      }
    }
  ]
}
```

You can also run the GUI version of the inspector and try out the tools. (Optional)

```text
> From the root of the favourites-mcp module, run:
npx @modelcontextprotocol/inspector \
  --config ./src/test/resources/mcp-servers-config.json \
  --server location
> Open the link from the console, the one including the token.
> In the GUI, you can see the tools after clicking the tools tab.
> Select the tool `add-favourite` and provide the input in JSON format. You can use the following input as an example:
  
  userId: "your-name-here"
  
  favouriteTalk: {
    "speakers": ["jettro","daniel"],
    "title": "From Scratch to Scalable: Building Smarter AI Agents with Frameworks"
  }
  
> Next, you can list all the favourites for your userId with the tool `list-favourites`.
```

Now we are ready to configure the MCP client and provide the client to the TalksAgent.

```
> Add the following configuration to the application.yml file in the web-app module.

spring:
  ai:
    mcp:
      client:
        enabled: true
        type: SYNC
        stdio:
          connections:
            location:
              command: java
              args:
                - -Dspring.ai.mcp.server.stdio=true
                - -jar
                - "${FAVOURITES_MCP_JAR:${user.dir}/favourites-mcp/target/favourites-mcp-0.0.1-SNAPSHOT.jar}"
```

MCP provided tools can be used by an Agent through the registration of a ToolCallbackProvider. The `SyncMcpToolCallbackProvider` that needs a list of `McpSyncClients`. These clients can be injected by Spring.

```
> Inject `List<McpSyncClient> mcpSyncClients` into configuration where the TalksAgent bean is defined (SpringAIAgentConfig).
> Inject the SyncMcpToolCallbackProvider into the constructor of the TalksAgent.
> In the method doInvoke, add the ToolCallbacks from the SyncMcpToolCallbackProvider to the chat client.
  - Tip: The fluent interface of the chatClient has a method `toolCallbacks` to add the tool callbacks.
> Fix all the compile errors.
> Change the prompt of the TalksAgent to include the following text or something similar:

You can also manage favourites for users. You can add talks and list them.
When a user asks for a list of favourite talks, they can filter the talks by speaker.

> Restart the application
> Ask the agent to add a talk to your favourites and list your favourite talks.
```

In the root of your project, you can find a folder `logs`. In this folder, you can find the log files of the application. The log files are rotated daily. You can use these logs to see what is happening in the application. Check the logs if something does not work as expected. (Hint: check the userId the agent is using).

Next to the folder logs, you can find a folder `data`. In this folder, the MCP server stores the favourite talks in a file called favourites.json. You can check this file to see if the favourites are stored correctly. What is the username that the favourites are stored under?

We want to fix the userId in the favourites store. We could add the user id to the prompt, but that would not be very secure. A better solution is to use the userId as the conversationId for the memory and the MCP client. This way, each user has its own memory and its own favourites.

```
> Check the classes `FixArgumentsSyncMcpToolCallback` and `ToolCallbackUtility` in the springai-agent module for an example of how to do this.
> Change the TalksAgent and the configuration to pass the ToolCallback[] instead of the SyncMcpToolCallbackProvider.
  - Tip: ToolCallbackUtility contains a method to create the ToolCallback[].
> Ask the agent to add all talks about embabel to your favourites. Check the logs and the favourites.json file to see if the userId is used correctly.
```

## Implement the remote MCP Server and client
Working with a local running MCP server is only realistic for a single user through a tool like Claude Desktop. Often the MCP server is a window to something remote, like Slack or Home automation. In this assignment, you will implement a remote MCP server and client. The remote MCP server will be a simple Spring Boot application that exposes the same tools as the favourites-mcp module. The remote MCP client will connect to the remote MCP server through SSE.

The remote MCP server is in the favourites-mcp-remote module. You need to make the changes to connect to the remote MCP server. The remote MCP server is an application that can be started like any spring boot application. As it is a special spring boot application, we can also expose web pages. Therefore, the remote MCP server has a few pages to monitor the favourites per user. You can visit the pages in the browser.

```text
> From the root of the project, run `mvn clean install -DskipTests -pl favourites-mcp-remote`
> Start the remote MCP server from the root of the favourites-mcp-remote, run:
mvn spring-boot:run
> Open the browser and go to http://localhost:8081. You should see a welcome page.
> Add a few favourites for multiple users. Go back to the list and check if the favourites are added correctly.
  - Note that you can filter the favourites by user, but in the web client you can also list all favourites. This is not possible through the MCP tool.
``` 

If you have the MCP inspector installed, you can also test the remote MCP server with the MCP inspector. (Optional)

```bash
npx @modelcontextprotocol/inspector
```

```text
> In the selector for Transport type, select `sse`. In the field Server URL, enter `http://localhost:8081/mcp/sse`. 
> Click Connect. You should see the tools after clicking the tools tab.
```

Next you have to change the web application to connect to the remote MCP server.

```text
> Find the MCP stdio configuration for the favourites
> Replace the stdio configuration with the following sse configuration:
```

```yaml
sse:
  connections:
    favourites:
      url: http://localhost:8081
      sse-endpoint: /sse
```

Now test adding favourites in the application. Use the Favourites web application to verify if the agent did a good job.