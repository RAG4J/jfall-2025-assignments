# Running with Embabel Agent Platform

Embabel takes a different approach to for instance Spring AI. With Embabel the preference is to work with rich domain objects. Through Actions you define what an agent can do. During the planning face, the input and output of actions is used to decide on the planning of actions. This means that the LLM is not used to decide what action to take, but the LLM is used to fill in the input parameters of actions.

When working with Embabel, the logs are your friend. The logs show you the planning of the agent, the actions taken and the input and output of actions. This makes it easy to debug and understand what is happening.

Lets configure the application to use Embabel and run the Conference talks agent.

```text
> configure the profile `embabel` in `application.yml` as active profile
> uncomment the embabel dependency in the `pom.xml` from the `web-app` module
> ./mvnw clean install
```

Embabel requires environment variables to configure the connection to OpenAI. You can set these in your terminal or in your IDE run configuration. Beware, the url needs to be the url of your proxy, but append `/openai` to the url. If you use your own OpenAI account, you can omit the base url.

```bash
export OPENAI_API_KEY=your_api_key_here
export OPENAI_BASE_URL=the_url_of_your_proxy_or_openai
```

You can verify the token in the token page of the web application. It throws an exception with an active embabel profile if the token is not valid.

```text
> Start the application
  - ./mvnw -pl web-app spring-boot:run
  - If you see a WARN message about the token, make sure the token in the application.yml and the environment are the same.
> Open http://localhost:8080 in your browser
> Ask a question about one of the conference talks.
  - You should see a message that no talks are found, this is because the tools are not configured.
  - It also happens that the agent just makes up an answer, this is because the agent does not have any tools to search for talks. You can change the prompt to tell the agent to say that it does not know if it does not have enough information.
```

Look at the class `EmbabelAgent`, this is the class that interacts with the Embabel agents through the `Autonomy` class. This class has the capability to look at the question and choose the right agent to answer the question. The `Autonomy` class is the main entry point to interact with Embabel agents.

## Tools

Next, we provide the tools to the agent to be able to search for conference talks. The tools bean is defined in the `EmbabelAgentConfig` class. The `EmbabelConferenceTools` is the bean containing the tools.

```text
> Provide the `EmbabelConferenceTools` bean to the TalksAgent.
  - The bean `EmbabelConferenceTools` is available in the `EmbabelAgentConfig` class
  - Note the unit test `TalksAgentTest` that shows how to test the agent with the tools. Uncomment the lines and make the test pass.
> Register the tools as an object to the LLM call.
  - Embabel has a method `withToolObject` that you can use to register the tools.
> Restart the application and ask a question about one of the conference talks.
```

You should now be able to ask questions about the conference talks and get answers. The agent will use the tools to search for the talks and provide the answer.

## Memory

We do not have this for the Embabel Agent at this time. 

## Marking favourite talks through MCP

You should have the MCP configuration still in your application.yml. If not, add it again.

```yaml
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

Embabel has a mechanism called ToolGroups. A ToolGroup is a collection of tools that can be used together. The idea is that you can ask for a ToolGroup with the role favourites and the agent will use the tools in that group to answer the question. If you provide a new mcp server for favourites, you do not need to change the agent. Through the ToolGroup you can also limit the methods that are exposed.

```text
> Provide the toolgroup to the TalksAgent `answerQuestion` action.
  - The `Action` annotation has a parameter `toolGroups` that you can use to provide the toolgroup. The value is the role of the toolgroup.
> Restart the application and ask a question about marking a talk as favourite.
  - You can ask questions like:
  - "Could you mark talk all talks about embabel as my favourite?"
> Check if the favourite is stored in the `favourites.json` file.
  - What is the userId that is used to store the favourites?
```

Providing a user id is a more advanced topic when using MCP. In the SpringAI project, you learned about a hack to force the userId parameter passed to the mcp server to be our user. In Embabel we take a less secure approach. We tell the LLM to what userId to use. So this is not a production ready approach, but it works for the workshop.

```text
> Create a new Action in the TalksAgent.
  - The output of the action is the object `UserId`. Fetch the userId from the `RequestIdentity` object.
> Add the UserId object as an argument to the `answerQuestion` action.
> Change the prompt to include the userId.
> Restart the application and ask a question about marking a talk as favourite.
> Check if the userId is now correct in the `favourites.json` file.
```

## Multi-agent

We still have the questions about Science Fiction. Use the same approach as the TalksAgent to create a SciFiAgent. The SciFiAgent has no tools, so it is a bit simpler.

```text
> Create a new class `SciFiAgent`.
  - The class should be similar to the `TalksAgent`
> Uncomment the tests in the `SciFiAgentTest` class and make them pass.
> Restart the application and ask a question about Science Fiction.
  - You should get an answer about Science Fiction.
```

