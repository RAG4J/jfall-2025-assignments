package org.rag4j.agent.mcp;

import org.rag4j.agent.mcp.model.FavouriteTalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

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

    /**
     * Web-only method to list favourites with support for showing all users' favourites
     * This is NOT exposed as an MCP tool
     */
    public List<FavouriteTalkWithUser> listFavouritesForWeb(String userId, String speakerNameFilter) {
        List<FavouriteTalkWithUser> results = new ArrayList<>();
        
        if (userId == null || userId.trim().isEmpty()) {
            // Show all favourites from all users
            logger.info("Listing favourites for all users");
            Map<String, List<FavouriteTalk>> allFavourites = favouritesRepository.getAllFavourites();
            
            for (Map.Entry<String, List<FavouriteTalk>> entry : allFavourites.entrySet()) {
                String currentUserId = entry.getKey();
                List<FavouriteTalk> userFavourites = entry.getValue();
                
                for (FavouriteTalk talk : userFavourites) {
                    boolean matchesFilter = true;
                    if (speakerNameFilter != null && !speakerNameFilter.trim().isEmpty()) {
                        String filter = speakerNameFilter.toLowerCase().trim();
                        matchesFilter = false;
                        for (String speaker : talk.speakers()) {
                            if (speaker.toLowerCase().contains(filter)) {
                                matchesFilter = true;
                                break;
                            }
                        }
                    }
                    
                    if (matchesFilter) {
                        results.add(new FavouriteTalkWithUser(talk.title(), talk.speakers(), currentUserId));
                    }
                }
            }
        } else {
            // Show favourites for specific user (same as MCP tool but wrapped with user info)
            logger.info("Listing favourites for user: {}", userId);
            List<FavouriteTalk> userFavourites = listFavourites(userId, speakerNameFilter);
            for (FavouriteTalk talk : userFavourites) {
                results.add(new FavouriteTalkWithUser(talk.title(), talk.speakers(), userId));
            }
        }
        
        return results;
    }

    /**
     * Record for favourites with user information (web-only)
     */
    public record FavouriteTalkWithUser(String title, String[] speakers, String userId) {
    }
}
