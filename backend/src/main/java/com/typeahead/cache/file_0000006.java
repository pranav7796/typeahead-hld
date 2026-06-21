package com.typeahead.cache;

import com.typeahead.hashing.CacheNode;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Owns one {@link RedisCacheNodeClient} per cache node and their lifecycle.
 * Lookups are by node id (the value returned by the hash router).
 */
public class RedisNodeClients {

    private final Map<String, RedisCacheNodeClient> clientsByNodeId = new LinkedHashMap<>();

    public RedisNodeClients(List<CacheNode> cacheNodes) {
        for (CacheNode node : cacheNodes) {
            clientsByNodeId.put(node.id(), new RedisCacheNodeClient(node));
        }
    }

    public RedisCacheNodeClient clientFor(CacheNode node) {
        return clientsByNodeId.get(node.id());
    }

    public void shutdown() {
        clientsByNodeId.values().forEach(RedisCacheNodeClient::close);
    }
}
