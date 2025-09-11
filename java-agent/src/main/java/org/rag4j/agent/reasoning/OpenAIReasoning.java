package org.rag4j.agent.reasoning;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;
import com.openai.models.ChatModel;
import com.openai.models.chat.completions.ChatCompletion;
import com.openai.models.chat.completions.ChatCompletionCreateParams;
import com.openai.models.chat.completions.ChatCompletionMessage;
import org.rag4j.agent.core.Conversation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

import static org.rag4j.agent.core.Sender.ASSISTANT;
import static org.rag4j.agent.core.Sender.USER;

/**
 * OpenAIReasoning is a service that interacts with the OpenAI API to perform reasoning tasks.
 * It uses the OpenAIClient to send messages and receive responses from the OpenAI model.
 */
public class OpenAIReasoning implements Reasoning {
    private static final Logger logger = LoggerFactory.getLogger(OpenAIReasoning.class);

    private final OpenAIClient openAIClient;
    private final ChatModel chatModel;
    private final SystemPrompt systemPrompt;

    public OpenAIReasoning(OpenAIClient openAIClient, SystemPrompt systemPrompt) {
        this.openAIClient = openAIClient;
        this.chatModel = ChatModel.GPT_4_1_MINI;
        this.systemPrompt = systemPrompt;
    }

    public Conversation.Message reason(Conversation.Message userMessage, Conversation conversation) {
        String outputMessage = callLlm(userMessage, conversation);
        logger.debug("Received output message: {}", outputMessage);

        return new Conversation.Message(outputMessage, ASSISTANT);
    }

    private String callLlm(Conversation.Message userMessage, Conversation conversation) {
        ChatCompletionCreateParams.Builder createParamsBuilder = ChatCompletionCreateParams.builder()
                .model(this.chatModel)
                .addDeveloperMessage(systemPrompt.build());

        prepareMessages(userMessage, conversation, createParamsBuilder);

        ChatCompletion chatCompletion = this.openAIClient.chat().completions().create(createParamsBuilder.build());
        List<ChatCompletionMessage> messages =
                chatCompletion.choices().stream()
                        .map(ChatCompletion.Choice::message)
                        .toList();

        List<String> output = messages.stream()
                .flatMap(message -> message.content().stream())
                .toList();
        // Check if the message is an action
        if (output.isEmpty()) {
            throw new IllegalStateException("No output received from OpenAI API.");
        }
        if (output.size() > 1) {
            logger.warn("Multiple messages received from OpenAI API, using the first one.");
        }

        String output_message = output.getFirst();
        logger.info("Output message: {}", output_message);
        return output_message;
    }

    private static void prepareMessages(Conversation.Message userMessage,
                                        Conversation conversation,
                                        ChatCompletionCreateParams.Builder createParamsBuilder) {
        for (Conversation.Message message : conversation.messages()) {
            if (message.sender().equals(USER)) {
                createParamsBuilder.addUserMessage(message.content());
            } else if (message.sender().equals(ASSISTANT)) {
                createParamsBuilder.addAssistantMessage(message.content());
            }
        }

        createParamsBuilder.addUserMessage(userMessage.content());
    }
}
