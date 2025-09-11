# Implement a Spring AI Agent

## Initialising the Spring AI agent
If you followed along with step 1, you already have the OpenAI API key configured in the application.yml within the web project. Look at the beginning of step 1 if you need help with this.

First we need to enable the Spring AI agent. This is done by changing the active profile in application.yml to 'springai'.

```
> In the pom.xml of the web project, add the dependency to the springai-agent module. You can disable the dependency to the java-agent module by commenting it out.
> Change the active profile in application.yml to 'springai'
> Restart the application
> Ask a question about one of the speakers of the conference.
```

You should get an answer like `The Agent could not create an answer to your question.`. Check the logs for messages similar to these.

```
2025-09-04T14:45:00.178+02:00  INFO 34109 --- [agent-workshop-assignments] [nio-8080-exec-1] org.rag4j.webapp.ChatController          : Received chat message: What talk is jettro giving from jettro
2025-09-04T14:45:00.179+02:00  INFO 34109 --- [agent-workshop-assignments] [nio-8080-exec-1] org.rag4j.agent.springai.TalksAgent      : SpringAIAgent invoke userId = jettro, userMessage = Message[content=What talk is jettro giving, sender=User]
2025-09-04T14:45:04.178+02:00  INFO 34109 --- [agent-workshop-assignments] [nio-8080-exec-1] org.rag4j.agent.springai.TalksAgent      : SpringAIAgent invoke content = Could you please specify which conference you are referring to? This will help me find the correct information about the talk Jettro is giving.
```

## Add Tools to the Spring AI Agent
First look at the classes `ActionAgent` and `TalksAgent` in the springai-agent module. The ActionAgent is a base class for agents that use actions (tools). The TalksAgent extends the ActionAgent and configures the actions it can use.

Look at the class `ConferenceTalksTools`. Note the two methods annotated with `@Tool`. These methods define the tools the agent can use. The schema of the tool is derived from the method signature and annotations. The implementation of the tool is in the method body.

Next, you are adding the tools to the TalksAgent.

```
> In the TalksAgent class, add the ConferenceTalksTools bean to the constructor.
> Inject the ConferenceTalksTools into the TalksAgent.
  - Tip: The ConferenceTalksTools is already a Spring Bean as it is configured in the configuration class SpringAIConfigCommon.
> In the method `doInvoke`, add the injected conferenceTalksTools to the chatClient.
  - Tip: The fluent interface of the chatClient has a method `tools` to add the tools.
> Restart the application
> Open the chat page and ask again, now you should get an answer with some talks.
```

## Add memory to the Spring AI Agent
The Spring AI agent has built-in support for memory. We want the memory to work for all actions. Therefore you need to add memory to the ActionAgent. For Spring AI you need the classes `ChatMemory` and `MessageWindowChatMemory`.

When adding new features to the agent, you can use advisors. Advisors are classes that can modify the request to and response from the LLM. In this case, we use an advisor to add memory to the chat client. The advisor you need is called `MessageChatMemoryAdvisor`.

For the web-app to work with the different agent implementations, we have a wrapper agent interface. The interface contains the Conversation class. Spring AI does not know this class. The memory of Spring AI also uses different classes. Therefore we need to map the Conversation to the ChatMemory of Spring AI. The default implementation shows only the last user and assistant message. To see all the messages from the memory, you can create a mapper service. You can also copy our version for inspiration.

```
> Create a Bean of type ChatMemory in the configuration class SpringAIAgentConfig. A good implementation to start with is MessageWindowChatMemory.
> In the ActionAgent class, add a constructor parameter for ChatMemory and store it in a protected field.
> In the method doInvoke of the TalksAgent, add the MessageChatMemoryAdvisor to the advisors of the chatClient.
  - Tip: The fluent interface of the chatClient has a method `advisors` to add the advisors.
  - Tip: The MessageChatMemoryAdvisor needs the ChatMemory and the userId as the conversationId.
> Restart the application
> Ask two questions that can only be answers when the agent remembers the previous question.
> Change the user (without restart) and ask the last question again, the agent should not remember the previous question.
```

The method to convert the ChatMemory to the Conversation in `ActionAgent` can look like this:

```java
protected Conversation convertChatMemoryToConversation(ChatMemory chatMemory, String userId) {
    List<Conversation.Message> messages = new ArrayList<>();
    for  (Message message : chatMemory.get(userId)) {
        switch (message.getMessageType()) {
            case MessageType.USER:
                messages.add(new Conversation.Message(message.getText(), USER));
                break;
            case MessageType.ASSISTANT:
                messages.add(new Conversation.Message(message.getText(), ASSISTANT));
                break;
            default:
                throw new RuntimeException("Unknown message type: " + message.getMessageType());
        }
    }
    return new Conversation(messages);
}
```

```text
> Convert the complete memory to messages in the conversation.
  - Tip: Check the return value in the doInvoke method of the TalksAgent.
```

## Create a multi-agent setup

Now that you have a working agent, it is time to create a multi-agent setup. You make the second agent that only answers questions about Science Fiction. The agent should respond with "I don't know" if the question is not about Science Fiction.

Use the TalksAgent as the basis for the SciFiAgent. The SciFiAgent does not need tools, but it does need memory. You can write the prompt yourself, or use the prompt below.

```
> Switch the active profile to 'springai-multi' in the application.yml file
> Create the memory bean in the configuration class SpringAiMultiAgentConfig.
> Add the memory bean to the constructor of the TalksAgent. (if not done in the previous assignment)
> Complete the SciFiAgent class.
> Add the SciFiAgent to the configuration in SpringAiMultiAgentConfig and add the SciFiAgent to the Agentregistry.
> Restart the application
> Ask questions for both agents. Check the logs to see if the router agent routes the question to the correct agent.
```

Below is a prompt that can work for the SciFiAgent.
```text
You are a geek that knows everything about Science Fiction related topics and likes to answer questions about this.
Science Fiction is your only expertise, so you can not answer questions related to other topics.
If the question is about a non-scifi topic, just say you don't know anything about that subject.
```

