package org.rag4j.agent.springai;

import org.rag4j.agent.core.ConferenceTalk;
import org.rag4j.agent.core.ConferenceTalksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;

import java.util.List;

public record ConferenceTalksTools(ConferenceTalksRepository conferenceTalksRepository) {
    private static final Logger logger = LoggerFactory.getLogger(ConferenceTalksTools.class);

    @Tool(description = "Find a conference talk by its title.")
    public List<ConferenceTalk> findConferenceTalkByTitle(String title) {
        logger.info("Finding conference talk by title: {}", title);

        return this.conferenceTalksRepository.findTalksByTitle(title);
    }

    @Tool(description = "Find all conference talks by a specific speaker.")
    public List<ConferenceTalk> findConferenceTalksBySpeaker(String speakerName) {
        logger.info("Finding conference talks by speaker: {}", speakerName);

        return this.conferenceTalksRepository.findTalksBySpeaker(speakerName);
    }
}
