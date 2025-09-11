package org.rag4j.agent.mcp;

import org.rag4j.agent.mcp.model.FavouriteTalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FavouritesService {
    private static final Logger logger = LoggerFactory.getLogger(FavouritesService.class);

    private final FavouritesRepository favouritesRepository;

    public FavouritesService(FavouritesRepository favouritesRepository) {
        this.favouritesRepository = favouritesRepository;
    }

    @Tool(
            description = "Add a favourite talk for a user.",
            name = "add-favourite"
    )
    public String addFavourite(String userId, FavouriteTalk favouriteTalk) {
        logger.info("Adding favourite talk: {} for user: {}", favouriteTalk, userId);

        favouritesRepository.addFavourite(userId, favouriteTalk);

        return "Favourite talk '" + favouriteTalk.title() + "' by " + String.join(", ", favouriteTalk.speakers()) + " added for user " + userId;
    }

    @Tool(
            name = "list-favourites",
            description = "List all favourite talks for a user, with optional filtering by speaker name."
    )
    public List<FavouriteTalk> listFavourites(String userId, String speakerNameFilter) {
        logger.info("Listing favourites for user: {}", userId);
        List<FavouriteTalk> allFavourites = favouritesRepository.getFavourites(userId);

        if (speakerNameFilter != null && !speakerNameFilter.isEmpty()) {
            logger.info("Filtering favourites for user: {}, by speaker: {}", userId, speakerNameFilter);
            String filter = speakerNameFilter.toLowerCase();
            return allFavourites.stream()
                    .filter(talk -> {
                        for (String speaker : talk.speakers()) {
                            if (speaker.toLowerCase().contains(filter)) {
                                return true;
                            }
                        }
                        return false;
                    })
                    .toList();
        } else {
            return allFavourites;
        }
    }
}
