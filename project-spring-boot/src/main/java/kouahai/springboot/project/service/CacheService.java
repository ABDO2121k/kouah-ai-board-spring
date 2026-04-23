package kouahai.springboot.project.service;

import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

import kouahai.springboot.project.utils.cache.CacheNames;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * Service for managing cache operations
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CacheService {

    private final CacheManager cacheManager;

    /**
     * Clear all caches
     */
    public void clearAllCaches() {
        log.info("Clearing all caches");
        cacheManager.getCacheNames()
                .forEach(cacheName -> {
                    var cache = cacheManager.getCache(cacheName);
                    if (cache != null) {
                        cache.clear();
                        log.debug("Cleared cache: {}", cacheName);
                    }
                });
    }

    /**
     * Clear specific cache
     */
    public void clearCache(String cacheName) {
        log.info("Clearing cache: {}", cacheName);
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.clear();
        }
    }

    /**
     * Clear project-related caches
     */
    public void clearProjectCaches() {
        log.info("Clearing project-related caches");
        clearCache(CacheNames.PROJECTS);
        clearCache(CacheNames.PROJECT_MEMBERS);
        clearCache(CacheNames.SPRINTS);
    }

    /**
     * Evict specific cache entry
     */
    public void evictCacheEntry(String cacheName, Object key) {
        log.debug("Evicting cache entry - cache: {}, key: {}", cacheName, key);
        var cache = cacheManager.getCache(cacheName);
        if (cache != null) {
            cache.evict(key);
        }
    }
}
