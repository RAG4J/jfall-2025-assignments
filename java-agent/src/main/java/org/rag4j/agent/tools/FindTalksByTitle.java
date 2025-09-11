package org.rag4j.agent.tools;

import org.rag4j.agent.core.ConferenceTalk;
import org.rag4j.agent.core.ConferenceTalksRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

/**
 * Tool for finding conference talks by their title.
 * This tool queries the ConferenceTalksRepository to find talks associated with a specific title.
 */
public class FindTalksByTitle extends Tool {
    private static final Logger logger = LoggerFactory.getLogger(FindTalksByTitle.class);
    private final ConferenceTalksRepository conferenceTalksRepository;

    public FindTalksByTitle(ConferenceTalksRepository conferenceTalksRepository) {
        super("find_talk_by_title",
                "for obtaining conference talks by title",
                "{\"title\": \"string\"}");
        this.conferenceTalksRepository = conferenceTalksRepository;
    }

    @Override
    public String execute(String arguments) {
        String title = extractSingleArgument("title", arguments);
        if (title == null || title.isEmpty()) {
            logger.error("Title cannot be empty.");
            return "Title cannot be empty.";
        }
        logger.info("Finding talk by title: {}", title);
        List<ConferenceTalk> talksByTitle = this.conferenceTalksRepository.findTalksByTitle(title);
        if (talksByTitle.isEmpty()) {
            return "No talks found with the title: " + title;
        }
        // Create a string containing all the talks found with a short message telling these are the found talks
        StringBuilder response = new StringBuilder("Found talks with title '" + title + "':\n");
        for (ConferenceTalk talk : talksByTitle) {
            response.append(talk.toString());
            response.append("\n");
        }
        return response.toString();
    }


}
