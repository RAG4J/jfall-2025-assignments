package org.rag4j.agent.core;

import java.util.List;

public record ConferenceTalk(
        String title,
        String description,
        String track,
        String room,
        String time,
        List<Speaker> speakers
) {
}
