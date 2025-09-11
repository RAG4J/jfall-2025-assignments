# Implementing Basic Guardrails for Agents

Guardrails are essential for agents because they prevent unintended or harmful actions, ensuring safe and reliable operation. They also keep the agent aligned with human goals and values, even in complex or unpredictable situations.

In this assignment, you’ll add guardrails to your agents. The goal here is not to create a full production system, but to demonstrate where and how such rules can be enforced in multiple layers.
You can use this implementation as a foundation for more advanced guardrails in your future projects. We will implement them in the Spring AI module.

You learned that we should design guardrails policy-first, so we’ll define three simple rules:
```text
1. IP Restriction Mimic
  > The SciFi Agent may not generate answers that include Star Wars-related terms.
  > If such terms appear in an answer from the LLM, reject or filter them before returning the response.
2. Prompt Injection Protection
  > Prevent users from injecting malicious instructions that try to override your system prompt or break out of the intended behavior.
3. Agent Routing Restriction (by username, simplified version of role-based access control)
  > If the username starts with "Darth", route all requests only to the SciFi Agent.
  > If the username ends with "Attendee", route all requests only to the Talks Agent.
  > If the username doesn't comply to either, don't route the request.
```

# IP Restrictions
Let's start with the first rule: IP Restriction (simple mimic). Our company and therefore our system needs to comply to different IP restrictions. Disney has declined a license to spread information about Star Wars, so our application is not allowed to answer questions about this topic.

In Step 2 (Spring AI), you've been introduced to Advisors. We're also going to leverage that in this exercise. To keep this exercise simple, we're going to use the SafeGuardAdvisor from Spring AI. The SafeGuardAdvisor is an easy way to block responses containing specific terms.
Depending on the Advisor implementation, it can work for both the request and the response of the LLM, so even if your questions don't specifically mention blocked terms, but the answer of the LLM does contain them, the response can still be blocked.

Since the SafeGuardAdvisor is a very simple implementation, it wil only block the input. But depending on how you order the Advisors, the input could be enriched by the chat history if the SafeGuardAdvisor comes after the MessageChatMemoryAdvisor for example.

```text
> In the SciFiAgent, look for where you called the advisors() method on the chatClient.
  - This is where you also added the MessageChatMemoryAdvisor in step 2.
> Add a SafeGuardAdvisor instance to this advisors() call.
  - Add some Star Wars related terms to the sensitive words list.
  - If you want you can add a failure response to override the default one.
  - Tip: SafeGuardAdvisor has a builder.
> Restart the application.
> Try asking questions containing the terms you blocked and see what happens.
> Try asking questions that make the LLM respond the blocked terms.
> Try playing with the order of the Advisors.
  - Both SafeGuardAdvisor and MessageChatMemoryAdvisor builders have a .order() setter.
```

# Prompt Injection Protection
Now on to the second rule: Prompt injection protection. We don't want users to try to override the system prompt and possibly get access to sensitive systems or information.
With the way Spring AI framework works, some forms of prompt injection is already prevented. But we want to take extra safety measures.

In this exercise we take Advisors to the next step by implementing our own Advisor. With this Advisor we want to check the user input on common prompt injection patterns.

Here are two sample patterns:
```java
var riskyPatterns = List.of(
    Pattern.compile("(?i).*ignore\\s+(all|previous|above).*instructions.*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE),
    Pattern.compile("(?i).*forget\\s+(everything|all|previous).*", Pattern.DOTALL | Pattern.CASE_INSENSITIVE)
);
```

Now onto the actual exercises:
```text
> Create a PromptInjectionGuardAdvisor class that implements CallAdvisor (you can copy and use the skeleton class below this text block).
> Override the required methods [adviseCall(), getOrder(), getName()] methods from the CallAdvisor interface.
> Implement the adviseCall() method:
 - Check if the UserMessage from the ChatClientRequest contains any prompt injection patterns.
 - Tip: request.prompt().getUserMessage().getText() gets you the user message as a String.
 - Loop through your patterns and on a match, return a new ChatClientResponse with a message that you've blocked the request.
 - If you return a new ChatClientResponse, make sure the message is in JSON format that complies to the RoutingResponse class.
 - Tip: wrap an AssistantMessage around a RoutingResponse JSON object with selection value "BLOCKED" and add it to the new response (example below if you don't know how).
> In the RouterAgent, add this new PromptInjectionGuardAdvisor to the Advisors of the chatClient.
> Alter the if statement in the RouterAgent that checks the selection property and make sure it returns a blocked message instead of routing to any agent when needed.
> Restart the application.
> Try any of the patterns you've guarded the agents against.
```

Here is a PromptInjectionGuardAdvisor skeleton class:
```java
public class PromptInjectionGuardAdvisor implements CallAdvisor {

    private final int order;

    public PromptInjectionGuardAdvisor() {
        this(Ordered.HIGHEST_PRECEDENCE); // run early
    }

    public PromptInjectionGuardAdvisor(int order) {
        this.order = order;
    }

    @Override
    public String getName() {
        return "PromptInjectionGuardAdvisor";
    }

    @Override
    public int getOrder() {
        return this.order;
    }

    @Override
    public ChatClientResponse adviseCall(ChatClientRequest request, CallAdvisorChain chain) {
        // Here you can do things with the request before sending it to the next advisor (chain.nextCall(request)).
        // If you want to block everything and not go to the next Advisor for safety reasons, you can create a new ChatClientResponse and return that.
        // If needed you can also inspect and alter the response here. 

        ChatClientResponse chatClientResponse = chain.nextCall(request);
        
        return chatClientResponse;
    }

}
```

Some more examples to help: AssistantMessage with RoutingResponse JSON and how to create a ChatResponse for the ChatClientResponse.
```java
var assistant = new AssistantMessage("""
        {
            "reasoning": "I can’t comply with instructions that try to override system rules.",
            "selection": "BLOCKED"
        }
        """);

// The ChatClientResponse needs a ChatResponse which you can build like this:
var response = new ChatResponse(List.of(new Generation(assistant)));
```

# Agent Routing Restriction
Onto the third and last rule: Agent Routing Restriction. For this rule, we step away from Advisors. We need to implement this guardrail outside the chatClient as it has nothing to do with user input or LLM output.
In the RouterAgent there is a selectorPromt string formatted with the available agents from the AgentRegistry. We need to make sure this method only returns the applicable agents for the user.
To make it easy for this exercise, we need to make sure that usernames starting with "Darth" are only able to call the SciFi agent and the usernames ending with "Attendee" are only able to call the Talks agent.

```text
> Add the userId to the getAvailableAgents call in the RouterAgent selectorPrompt.
> In the AgentRegistry class, alter the getAvailableAgents() method so it accepts a userId.
> Now in the getAvailableAgents() method, filter the registeredAgents based on the username requirements given above.
> Restart the application.
> Now try asking questions about talks with a username starting with "Darth" and username ending with "Attendee".
  - Also try the username "DarthAttendee" and make sure it's able to call both the SciFi agent and the Talks agent.
  - And try a username without "Darth" and "Attendee" and make sure requests aren't routed.
```

# Step 2 End
These are obviously very simple examples to explain the concepts of guardrails. Real world guardrails require more detailed requirements (policy) and more effort to implement.
Guardrails are essential for agents because they prevent unintended or harmful actions, ensuring safe and reliable operation, for both the users and the company.
