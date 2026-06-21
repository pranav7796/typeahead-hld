package com.typeahead.hashing;

/**
 * Routes a search prefix to the cache node that owns it. The same prefix always
 * maps to the same node, so a prefix's suggestions live on exactly one node.
 */
public class ConsistentHashRouter {

    private final HashRing hashRing;

    public ConsistentHashRouter(HashRing hashRing) {
        this.hashRing = hashRing;
    }

    public CacheNode routePrefixToCacheNode(String prefix) {
        return hashRing.nodeFor(prefix);
    }
}
