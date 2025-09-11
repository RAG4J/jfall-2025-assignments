package org.rag4j.agent.core;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class ConferenceTalksRepository {
    private static final Logger logger = LoggerFactory.getLogger(ConferenceTalksRepository.class);

    private final List<ConferenceTalk> talks;

    public ConferenceTalksRepository() {
        try {
            this.talks = parseTalksFromJson();
        } catch (IOException e) {
            logger.error("Failed to parse talks from JSON", e);
            throw new RuntimeException(e);
        }
    }

    public List<ConferenceTalk> findTalksByTitle(String title) {
        return talks.stream()
                .filter(talk -> talk.title().toLowerCase().contains(title.toLowerCase()))
                .toList();
    }

    public List<ConferenceTalk> findTalksBySpeaker(String speaker) {
        return talks.stream()
                .filter(talk -> talk.speakers().stream()
                        .anyMatch(talkSpeaker -> talkSpeaker.name().toLowerCase().contains(speaker.toLowerCase())))
                .toList();
    }

    public static List<ConferenceTalk> parseTalksFromJson() throws IOException {
        ObjectMapper mapper = new ObjectMapper();
        List<ConferenceTalk> talks = new ArrayList<>();
        try (InputStream is = ConferenceTalksRepository.class.getClassLoader().getResourceAsStream("talks.json")) {
            if (is == null) {
                throw new IOException("talks.json not found in resources");
            }
            JsonNode root = mapper.readTree(is);
            for (JsonNode node : root) {
                String title = node.path("title").asText();
                String description = node.path("description").asText();
                String track = node.path("track").asText();
                String level = node.path("level").asText();
                List<Speaker> speakers = new ArrayList<>();
                for (JsonNode speakerNode : node.path("speakers")) {
                    speakers.add(new Speaker(speakerNode.asText()));
                }
                talks.add(new ConferenceTalk(title, description, track, level, speakers));
            }
        }
        return talks;
    }
}
