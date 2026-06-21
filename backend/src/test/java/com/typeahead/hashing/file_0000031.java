package com.typeahead.hashing;

import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ConsistentHashRouterTest {

    private static final List<CacheNode> NODES = List.of(
            new CacheNode("node-1", "host-1", 6379),
            new CacheNode("node-2", "host-2", 6379),
            new CacheNode("node-3", "host-3", 6379));

    private final ConsistentHashRouter router = new ConsistentHashRouter(new HashRing(NODES));

    @Test
    void routesSamePrefixToSameNode() {
        CacheNode first = router.routePrefixToCacheNode("iph");
        for (int i = 0; i < 100; i++) {
            assertEquals(first, router.routePrefixToCacheNode("iph"),
                    "Same prefix must always route to the same node");
        }
    }

    @Test
    void distributesPrefixesAcrossAllNodes() {
        Map<String, Integer> hitsByNode = new HashMap<>();
        for (int i = 0; i < 10_000; i++) {
            CacheNode node = router.routePrefixToCacheNode("prefix-" + i);
            hitsByNode.merge(node.id(), 1, Integer::sum);
        }
        assertEquals(3, hitsByNode.size(), "All three nodes should receive traffic");
        // No node should be wildly over-loaded (sanity check on the ring balance).
        hitsByNode.values().forEach(count ->
                assertTrue(count > 1_500, "Each node should get a reasonable share: " + hitsByNode));
    }
}
