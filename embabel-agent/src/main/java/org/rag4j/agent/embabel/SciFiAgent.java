package org.rag4j.agent.embabel;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.config.models.OpenAiModels;
import com.embabel.agent.domain.io.UserInput;
import org.rag4j.agent.core.Conversation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Agent(name = "SciFiAgent",
        description = "An agent specialized in science fiction topics that can answer questions about SciFi and write" +
                " short stories about science fiction characters.",
        version = "1.0.0")
public record SciFiAgent() {
    private static final Logger logger = LoggerFactory.getLogger(SciFiAgent.class);

    @AchievesGoal(
            description = "Answers questions about science fiction or writes short stories about SciFi characters " +
                    "based on user requests."
    )
    @Action
    public Conversation handleSciFiRequest(UserInput question, OperationContext context) {
        logger.info("SciFiAgent processing request: {}", question.getContent());

        Conversation response = context.ai().withLlm(OpenAiModels.GPT_41_MINI)
                .createObject(String.format("""
                                You are a science fiction expert and creative writer.
                                You can answer questions about science fiction literature, movies, TV shows,
                                concepts, and technologies. You can also write engaging short stories
                                featuring science fiction characters and themes.
                                
                                When answering questions, be informative and engaging, drawing from
                                the rich history of science fiction.
                                
                                When writing stories, be creative and immersive, incorporating
                                classic SciFi elements like advanced technology, space exploration,
                                time travel, alien encounters, dystopian societies, or futuristic scenarios.
                                
                                # User Request
                                %s
                                """,
                        question.getContent()
                ).trim(), Conversation.class);

        logger.info("SciFi response generated: {}", response.messages().getFirst().content());
        return response;
    }
}
