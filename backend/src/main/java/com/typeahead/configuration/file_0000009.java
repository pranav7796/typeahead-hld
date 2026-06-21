package com.typeahead.configuration;

import com.typeahead.cache.RedisNodeClients;
import com.typeahead.hashing.CacheNode;
import com.typeahead.hashing.ConsistentHashRouter;
import com.typeahead.hashing.HashRing;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds the distributed cache: one client per Redis node plus the consistent
 * hash router. Node addresses come from {@code typeahead.redis.nodes} config.
 */
@Configuration
@ConfigurationProperties(prefix = "typeahead.redis")
public class RedisClusterConfiguration {

    /** Bound from application.yml; populated by Spring before the @Bean methods run. */
    private List<NodeConfig> nodes = new ArrayList<>();

    public List<NodeConfig> getNodes() {
        return nodes;
    }

    public void setNodes(List<NodeConfig> nodes) {
        this.nodes = nodes;
    }

    private List<CacheNode> cacheNodes() {
        return nodes.stream()
                .map(n -> new CacheNode(n.getId(), n.getHost(), n.getPort()))
                .toList();
    }

    @Bean(destroyMethod = "shutdown")
    public RedisNodeClients redisNodeClients() {
        return new RedisNodeClients(cacheNodes());
    }

    @Bean
    public ConsistentHashRouter consistentHashRouter() {
        return new ConsistentHashRouter(new HashRing(cacheNodes()));
    }

    /** One Redis node's address, bound from config. */
    public static class NodeConfig {
        private String id;
        private String host;
        private int port;

        public String getId() {
            return id;
        }

        public void setId(String id) {
            this.id = id;
        }

        public String getHost() {
            return host;
        }

        public void setHost(String host) {
            this.host = host;
        }

        public int getPort() {
            return port;
        }

        public void setPort(int port) {
            this.port = port;
        }
    }
}
