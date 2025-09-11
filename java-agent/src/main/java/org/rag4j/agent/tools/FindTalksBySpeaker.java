package org.rag4j.agent.tools;

import org.rag4j.agent.core.ConferenceTalk;
import org.rag4j.agent.core.ConferenceTalksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Tool for finding conference talks by speaker.
 * This tool queries the ConferenceTalksRepository to find talks associated with a specific speaker.
 */
public class FindTalksBySpeaker extends Tool {
    private static final Logger logger = LoggerFactory.getLogger(FindTalksBySpeaker.class);
    private final ConferenceTalksRepository conferenceTalksRepository;

    public FindTalksBySpeaker(ConferenceTalksRepository conferenceTalksRepository) {
        super("find_talk_by_speaker",
                "for obtaining conference talks by speaker",
                "{\"speaker\": \"string\"}");
        this.conferenceTalksRepository = conferenceTalksRepository;
    }

    @Override
    public String execute(String arguments) {
        String speaker = extractSingleArgument("speaker", arguments);
        if (speaker == null || speaker.isEmpty()) {
            logger.error("Speaker name cannot be empty.");
            return "Speaker name cannot be empty.";
        }
        logger.info("Finding talk by speaker: {}", speaker);
        List<ConferenceTalk> talksBySpeaker = this.conferenceTalksRepository.findTalksBySpeaker(speaker);
        if (talksBySpeaker.isEmpty()) {
            return "No talks found with the speaker: " + speaker;
        }
        // Create a string containing all the talks found with a short message telling these are the found talks
        StringBuilder response = new StringBuilder("Found talks with speaker '" + speaker + "':\n");
        for (ConferenceTalk talk : talksBySpeaker) {
            response.append(talk.toString());
            response.append("\n");
        }
        return response.toString();
    }

}
