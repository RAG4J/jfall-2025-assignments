package org.rag4j.agent.embabel;

import com.embabel.agent.domain.io.UserInput;
import com.embabel.agent.testing.unit.FakeOperationContext;
import com.embabel.agent.testing.unit.FakePromptRunner;
import org.junit.jupiter.api.Test;
import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.core.Sender;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertTrue;

public class SciFiAgentTest {
    
//    @Test
//    void testSciFiAgentAnswersQuestion() {
//        var context = FakeOperationContext.create();
//        var promptRunner = (FakePromptRunner) context.promptRunner();
//
//        context.expectResponse(new Conversation(List.of(new Conversation.Message(
//                "Isaac Asimov is one of the most influential science fiction writers of all time, " +
//                        "best known for his Robot series and Foundation series. He formulated the famous " +
//                        "Three Laws of Robotics, which have become a cornerstone concept in science fiction. " +
//                        "His works explore themes of artificial intelligence, psychohistory, and the future " +
//                        "of human civilization.",
//                Sender.ASSISTANT
//        ))));
//
//        var agent = new SciFiAgent();
//
//        agent.handleSciFiRequest(
//                new UserInput("Who is Isaac Asimov and what are his contributions to science fiction?"),
//                context
//        );
//
//        String prompt = promptRunner.getLlmInvocations().getFirst().getPrompt();
//        assertTrue(prompt.contains("Isaac Asimov"), "Expected prompt to contain 'Isaac Asimov'");
//        assertTrue(prompt.contains("science fiction expert"), "Expected prompt to contain agent description");
//    }

//    @Test
//    void testSciFiAgentWritesShortStory() {
//        var context = FakeOperationContext.create();
//        var promptRunner = (FakePromptRunner) context.promptRunner();
//
//        context.expectResponse(new Conversation(List.of(new Conversation.Message(
//                "Commander Zara checked her neural interface one last time before stepping into the " +
//                        "teleportation chamber. The mission to Alpha Centauri would require all her courage, " +
//                        "especially knowing that the alien signals they'd detected suggested an intelligence " +
//                        "far beyond human comprehension. As the quantum field enveloped her, she wondered " +
//                        "if humanity was ready for first contact.",
//                Sender.ASSISTANT
//        ))));
//
//        var agent = new SciFiAgent();
//
//        agent.handleSciFiRequest(
//                new UserInput("Write a short story about a space commander encountering aliens"),
//                context
//        );
//
//        String prompt = promptRunner.getLlmInvocations().getFirst().getPrompt();
//        assertTrue(prompt.contains("space commander"), "Expected prompt to contain 'space commander'");
//        assertTrue(prompt.contains("write engaging short stories"), "Expected prompt to contain story writing instruction");
//        assertTrue(prompt.contains("creative writer"), "Expected prompt to contain creative writer description");
//    }

//    @Test
//    void testSciFiAgentHandlesTechnologyQuestion() {
//        var context = FakeOperationContext.create();
//        var promptRunner = (FakePromptRunner) context.promptRunner();
//
//        context.expectResponse(new Conversation(List.of(new Conversation.Message(
//                "Time travel in science fiction typically involves concepts like wormholes, " +
//                        "time machines, or relativistic effects. Popular examples include H.G. Wells' " +
//                        "The Time Machine, Back to the Future's DeLorean, and the TARDIS from Doctor Who. " +
//                        "The genre explores paradoxes like the grandfather paradox and bootstrap paradox.",
//                Sender.ASSISTANT
//        ))));
//
//        var agent = new SciFiAgent();
//
//        agent.handleSciFiRequest(
//                new UserInput("Explain time travel concepts in science fiction"),
//                context
//        );
//
//        String prompt = promptRunner.getLlmInvocations().getFirst().getPrompt();
//        assertTrue(prompt.contains("time travel"), "Expected prompt to contain 'time travel'");
//        assertTrue(prompt.contains("science fiction"), "Expected prompt to contain 'science fiction'");
//    }
}
