package org.rag4j.agent.mcp;

import org.rag4j.agent.mcp.model.FavouriteRepositoryException;
import org.rag4j.agent.mcp.model.FavouriteTalk;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Repository;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.core.type.TypeReference;
import java.io.File;
import java.io.IOException;
import java.util.*;

@Repository
public class FavouritesRepository {
    private static final Logger logger = LoggerFactory.getLogger(FavouritesRepository.class);

    private static final String FAVOURITES_FILE = "favourites.json";
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final Map<String, List<FavouriteTalk>> userFavourites = new HashMap<>();
    private final File file;

    public FavouritesRepository() {
        File dataDir = new File("data");
        if (!dataDir.exists()) {
            boolean mkdirs = dataDir.mkdirs();
            if (!mkdirs) {
                throw new FavouriteRepositoryException("Failed to create data directory");
            }
        }
        this.file = new File(dataDir, FAVOURITES_FILE);

        loadFromDisk();
    }

    public synchronized List<FavouriteTalk> getFavourites(String userId) {
        return userFavourites.getOrDefault(userId, new ArrayList<>());
    }

    public synchronized void addFavourite(String userId, FavouriteTalk item) {
        List<FavouriteTalk> favs = userFavourites.computeIfAbsent(userId, k -> new ArrayList<>());
        if (!favs.contains(item)) {
            favs.add(item);
            saveToDisk();
        }
    }

    public synchronized void removeFavourite(String userId, FavouriteTalk item) {
        List<FavouriteTalk> favs = userFavourites.get(userId);
        if (favs != null && favs.remove(item)) {
            saveToDisk();
        }
    }

    private void loadFromDisk() {
        logger.info("Loading Favourites from disk...");
        if (file.exists()) {
            try {
                Map<String, List<FavouriteTalk>> data = objectMapper.readValue(file, new TypeReference<Map<String, List<FavouriteTalk>>>() {});
                userFavourites.putAll(data);
            } catch (IOException e) {
                logger.error("Failed to load favourites from disk", e);
                throw new FavouriteRepositoryException("Failed to load favourites from disk", e);
            }
        }
    }

    private void saveToDisk() {
        logger.info("Saving Favourites to disk...");
        try {
            objectMapper.writerWithDefaultPrettyPrinter().writeValue(file, userFavourites);
        } catch (IOException e) {
            logger.error("Failed to save favourites to disk", e);
            throw new FavouriteRepositoryException("Failed to save favourites to disk", e);
        }
    }
}
