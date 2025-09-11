package org.rag4j.evals.service;

import org.rag4j.evals.model.EvaluationScore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.prompt.ChatOptions;
import org.springframework.ai.openai.api.OpenAiApi;
import org.springframework.stereotype.Service;

@Service
public class LLMScoreService {
    private static final Logger logger = LoggerFactory.getLogger(LLMScoreService.class);
    private final ChatClient chatClient;

    public LLMScoreService(ChatClient chatClient) {
        this.chatClient = chatClient;
    }

    public EvaluationScore evaluate(String input, String response) {
        logger.info("Evaluate input = {} for response = {}", input, response);

        String systemPrompt = """
                You are an expert evaluator of AI-generated responses.
                Given an input question and an AI-generated response, you will provide a score and a reason for the score.
                The score should be one of the following:
                - GOOD: The response is accurate, relevant, and helpful.
                - BAD: The response is inaccurate, irrelevant, or unhelpful.
                
                Provide your answer in the following JSON format:
                {
                  "scoreType": "Good" or "Bad",
                  "reason": "A brief explanation of why you gave this score."
                }
                """;

        String userPrompt = """
                Input Question: %s
                AI-Generated Response: %s
                
                Please provide your evaluation in the specified JSON format.
                """.formatted(input, response);

        EvaluationScore content = this.chatClient.prompt()
                .options(ChatOptions.builder()
                        .model(OpenAiApi.ChatModel.GPT_4_1_MINI.getValue())
                        .build())
                .system(systemPrompt)
                .user(userPrompt)
                .call()
                .entity(EvaluationScore.class);

        assert content != null;
        logger.info("Input evaluation for input = {}, score = {}, reason = {}", input, content.scoreType(), content.reason());
        return content;
    }
}
