package org.rag4j.agent.embabel;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.testing.unit.FakeOperationContext;
import com.embabel.agent.testing.unit.FakePromptRunner;
import org.junit.jupiter.api.Test;
import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.embabel.model.UserId;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

public class TalksAgentTest {
    @Test
    void testTalksAgent() {
        var context = FakeOperationContext.create();
        var promptRunner = (FakePromptRunner) context.promptRunner();

        context.expectResponse(new Conversation(List.of(new Conversation.Message(
                """
                        Sure! Here are some talks by Alice Smith:
                        1. "Advancements in AI" - A deep dive into the latest trends and technologies in \
                        artificial intelligence.
                        2. "Machine Learning Basics" - An introductory session on machine learning concepts and \
                        applications.""",
                org.rag4j.agent.core.Sender.ASSISTANT
        ))));

//        EmbabelConferenceTools tools = mock(EmbabelConferenceTools.class);
        var agent = new TalksAgent();

        agent.answerQuestion(
                new UserInput("Can you give me all the talks from Alice Smith?"),
//                new UserId("the-user-id"),
                context
        );

        String prompt = promptRunner.getLlmInvocations().getFirst().getPrompt();
        assertTrue(prompt.contains("Alice Smith"), "Expected prompt to contain 'Alice Smith'");

    }
}
