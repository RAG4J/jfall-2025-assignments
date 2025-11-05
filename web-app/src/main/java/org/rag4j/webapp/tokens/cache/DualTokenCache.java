package org.rag4j.webapp.tokens.cache;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.rag4j.webapp.tokens.TokenCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.attribute.PosixFilePermission;
import java.time.Instant;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Token cache implementation that uses both in-memory and disk storage.
 * Thread-safe with read/write locks for concurrent access.
 */
@Component
public class DualTokenCache implements TokenCache {
    private static final Logger logger = LoggerFactory.getLogger(DualTokenCache.class);
    
    private final ReadWriteLock lock = new ReentrantReadWriteLock();
    private volatile CachedToken memoryCache;
    
    private final boolean diskCacheEnabled;
    private final String diskCacheLocation;
    private final ObjectMapper objectMapper;
    
    public DualTokenCache(
            @Value("${openai.proxy.cache.enabled:true}") boolean diskCacheEnabled,
            @Value("${openai.proxy.cache.location:${user.dir}/data/token-cache.json}") String diskCacheLocation
    ) {
        this.diskCacheEnabled = diskCacheEnabled;
        this.diskCacheLocation = diskCacheLocation;
        this.objectMapper = new ObjectMapper();
    }
    
    @Override
    public Optional<CachedToken> get() {
        lock.readLock().lock();
        try {
            // Try memory cache first
            if (memoryCache != null) {
                return Optional.of(memoryCache);
            }
        } finally {
            lock.readLock().unlock();
        }
        
        // Try disk cache if memory cache is empty
        if (diskCacheEnabled) {
            lock.writeLock().lock();
            try {
                // Double-check memory cache after acquiring write lock
                if (memoryCache != null) {
                    return Optional.of(memoryCache);
                }
                
                Optional<CachedToken> diskToken = loadFromDisk();
                if (diskToken.isPresent()) {
                    memoryCache = diskToken.get();
                    logger.debug("Loaded token from disk cache into memory");
                    return diskToken;
                }
            } finally {
                lock.writeLock().unlock();
            }
        }
        
        return Optional.empty();
    }
    
    @Override
    public void put(CachedToken token) {
        lock.writeLock().lock();
        try {
            memoryCache = token;
            logger.debug("Stored token in memory cache for user: {}", token.userId());
            
            if (diskCacheEnabled) {
                saveToDisk(token);
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public void clear() {
        lock.writeLock().lock();
        try {
            memoryCache = null;
            logger.debug("Cleared memory cache");
            
            if (diskCacheEnabled) {
                try {
                    Path path = Paths.get(diskCacheLocation);
                    if (Files.exists(path)) {
                        Files.delete(path);
                        logger.debug("Cleared disk cache: {}", diskCacheLocation);
                    }
                } catch (IOException e) {
                    logger.warn("Error clearing disk cache: {}", e.getMessage());
                }
            }
        } finally {
            lock.writeLock().unlock();
        }
    }
    
    @Override
    public boolean isValid(CachedToken token, int bufferMinutes) {
        if (token == null) {
            return false;
        }
        long bufferMillis = bufferMinutes * 60L * 1000L;
        long bufferTime = System.currentTimeMillis() + bufferMillis;
        return token.expiresAtMillis() > bufferTime;
    }
    
    private Optional<CachedToken> loadFromDisk() {
        try {
            Path path = Paths.get(diskCacheLocation);
            if (!Files.exists(path)) {
                logger.debug("Disk cache file does not exist: {}", diskCacheLocation);
                return Optional.empty();
            }
            
            String json = Files.readString(path);
            JsonNode node = objectMapper.readTree(json);
            
            CachedToken token = new CachedToken(
                node.get("token").asText(),
                node.get("userId").asText(),
                node.get("expiresAtMillis").asLong()
            );
            
            logger.debug("Loaded token from disk cache");
            return Optional.of(token);
        } catch (IOException e) {
            logger.warn("Error loading token from disk cache: {}", e.getMessage());
            return Optional.empty();
        }
    }
    
    private void saveToDisk(CachedToken token) {
        try {
            Path path = Paths.get(diskCacheLocation);
            Files.createDirectories(path.getParent());
            
            Map<String, Object> cacheData = new HashMap<>();
            cacheData.put("token", token.token());
            cacheData.put("userId", token.userId());
            cacheData.put("expiresAtMillis", token.expiresAtMillis());
            cacheData.put("savedAt", Instant.now().toString());
            
            String json = objectMapper.writerWithDefaultPrettyPrinter().writeValueAsString(cacheData);
            Files.writeString(path, json);
            
            // Set restrictive permissions (Unix/Mac only)
            try {
                Set<PosixFilePermission> perms = new HashSet<>();
                perms.add(PosixFilePermission.OWNER_READ);
                perms.add(PosixFilePermission.OWNER_WRITE);
                Files.setPosixFilePermissions(path, perms);
            } catch (UnsupportedOperationException e) {
                // Windows doesn't support POSIX permissions
                logger.debug("POSIX file permissions not supported on this platform");
            }
            
            logger.debug("Saved token to disk cache: {}", diskCacheLocation);
        } catch (IOException e) {
            logger.warn("Error saving token to disk cache: {}", e.getMessage());
        }
    }
}
