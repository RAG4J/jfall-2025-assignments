package org.rag4j.agent.core;

import java.util.List;

public record ConferenceTalk(
        String title,
        String description,
        String track,
        String level,
        List<Speaker> speakers
) {
}
