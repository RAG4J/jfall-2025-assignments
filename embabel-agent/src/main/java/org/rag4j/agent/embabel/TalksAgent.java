package org.rag4j.agent.embabel;

import com.embabel.agent.api.annotation.AchievesGoal;
import com.embabel.agent.api.annotation.Action;
import com.embabel.agent.api.annotation.Agent;
import com.embabel.agent.api.annotation.ToolGroup;
import com.embabel.agent.api.common.OperationContext;
import com.embabel.agent.api.models.OpenAiModels;
import com.embabel.agent.domain.io.UserInput;
import org.rag4j.agent.core.Conversation;
import org.rag4j.agent.core.RequestIdentity;
import org.rag4j.agent.embabel.model.UserId;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Agent(name = "TalksAgent",
        description = "An agent that answers questions about conference talks",
        version = "1.0.0")
public record TalksAgent(EmbabelConferenceTools conferenceTools) {
    private static final Logger logger = LoggerFactory.getLogger(TalksAgent.class);

    @Action
    public UserId identifyUser(OperationContext context) {
        logger.info("Identifying user with identity: {}", RequestIdentity.getUserId());
        // In a real implementation, you might look up the user in a database
        return new UserId(RequestIdentity.getUserId() != null ? RequestIdentity.getUserId() : "anonymous");
    }

    @AchievesGoal(
            description = "Answers a question about conference talks using tools to obtain the right talks."
    )
    @Action()
    @ToolGroup(role = "mcp-favourites")
    public Conversation answerQuestion(UserId userId, UserInput question, OperationContext context) {
        Conversation response = context.ai().withLlm(OpenAiModels.GPT_41_MINI)
                .withToolObject(conferenceTools)
                .createObject(String.format("""
                                 You will be given a question about conference talks.
                                 You have access to talks to search for conference talks.
                                 Your task is to answer the question using the information from the talks.
                                
                                 If the question is about favourites, use the favourite tools to get the information.
                                 You need a userId this is provided.
                                
                                 # User ID
                                 %s
                                
                                 # Question
                                 %s
                                
                                """,
                        userId.id(),
                        question.getContent()
                ).trim(), Conversation.class);
        logger.info("Response generated: {}", response.messages().getFirst().content());
        return response;
    }
}
