package com.typeahead.cache;

import com.typeahead.hashing.CacheNode;
import io.lettuce.core.RedisClient;
import io.lettuce.core.RedisURI;
import io.lettuce.core.SetArgs;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;

import java.time.Duration;

/** Thin wrapper around a single Redis node connection (get / set-with-ttl / delete). */
public class RedisCacheNodeClient {

    private final CacheNode cacheNode;
    private final RedisClient redisClient;
    private final StatefulRedisConnection<String, String> connection;

    public RedisCacheNodeClient(CacheNode cacheNode) {
        this.cacheNode = cacheNode;
        this.redisClient = RedisClient.create(RedisURI.create(cacheNode.host(), cacheNode.port()));
        this.connection = redisClient.connect();
    }

    public CacheNode cacheNode() {
        return cacheNode;
    }

    public String get(String key) {
        return commands().get(key);
    }

    public void setWithTtl(String key, String value, Duration ttl) {
        commands().set(key, value, SetArgs.Builder.px(ttl.toMillis()));
    }

    public void delete(String key) {
        commands().del(key);
    }

    private RedisCommands<String, String> commands() {
        return connection.sync();
    }

    public void close() {
        connection.close();
        redisClient.shutdown();
    }
}
