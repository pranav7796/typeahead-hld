package com.typeahead.cache;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.typeahead.configuration.Constants;
import com.typeahead.dto.Suggestion;
import com.typeahead.hashing.CacheNode;
import com.typeahead.hashing.ConsistentHashRouter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Optional;

/**
 * Cache-aside access to suggestion lists in Redis. The prefix is routed to its
 * owning node via consistent hashing. Any Redis failure is swallowed so the
 * read path degrades to PostgreSQL instead of failing.
 */
@Component
public class PrefixSuggestionCache {

    private static final Logger log = LoggerFactory.getLogger(PrefixSuggestionCache.class);
    private static final TypeReference<List<Suggestion>> SUGGESTION_LIST = new TypeReference<>() {
    };

    private final ConsistentHashRouter router;
    private final RedisNodeClients nodeClients;
    private final ObjectMapper objectMapper;

    public PrefixSuggestionCache(ConsistentHashRouter router,
                                 RedisNodeClients nodeClients,
                                 ObjectMapper objectMapper) {
        this.router = router;
        this.nodeClients = nodeClients;
        this.objectMapper = objectMapper;
    }

    /** Returns cached suggestions for the prefix, or empty on a cache miss / Redis error. */
    public Optional<List<Suggestion>> get(String prefix) {
        try {
            String value = clientFor(prefix).get(Constants.suggestionKey(prefix));
            if (value == null) {
                return Optional.empty();
            }
            return Optional.of(objectMapper.readValue(value, SUGGESTION_LIST));
        } catch (Exception e) {
            log.warn("Cache read failed for prefix '{}', falling back to DB: {}", prefix, e.getMessage());
            return Optional.empty();
        }
    }

    /** Stores suggestions for the prefix with the configured TTL. */
    public void populate(String prefix, List<Suggestion> suggestions) {
        try {
            String value = objectMapper.writeValueAsString(suggestions);
            clientFor(prefix).setWithTtl(Constants.suggestionKey(prefix), value, Constants.SUGGESTION_TTL);
        } catch (Exception e) {
            log.warn("Cache populate failed for prefix '{}': {}", prefix, e.getMessage());
        }
    }

    /** Removes the cached entry for a prefix (used after batch writes). */
    public void invalidatePrefix(String prefix) {
        try {
            clientFor(prefix).delete(Constants.suggestionKey(prefix));
        } catch (Exception e) {
            log.warn("Cache invalidate failed for prefix '{}': {}", prefix, e.getMessage());
        }
    }

    private RedisCacheNodeClient clientFor(String prefix) {
        CacheNode node = router.routePrefixToCacheNode(prefix);
        return nodeClients.clientFor(node);
    }
}
