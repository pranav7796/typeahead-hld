package com.typeahead.hashing;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

/**
 * Consistent hash ring over the cache nodes. Each node is placed at several
 * points on the ring (virtual points) so keys distribute evenly. Looking up a
 * key returns the first node clockwise from the key's hash.
 */
public class HashRing {

    private static final int VIRTUAL_POINTS_PER_NODE = 160;

    private final TreeMap<Long, CacheNode> ring = new TreeMap<>();

    public HashRing(List<CacheNode> nodes) {
        if (nodes == null || nodes.isEmpty()) {
            throw new IllegalArgumentException("HashRing requires at least one cache node");
        }
        for (CacheNode node : nodes) {
            for (int i = 0; i < VIRTUAL_POINTS_PER_NODE; i++) {
                ring.put(hash(node.id() + "#" + i), node);
            }
        }
    }

    /** Returns the node that owns the given key (first node clockwise). */
    public CacheNode nodeFor(String key) {
        long keyHash = hash(key);
        Map.Entry<Long, CacheNode> entry = ring.ceilingEntry(keyHash);
        if (entry == null) {
            entry = ring.firstEntry(); // wrap around the ring
        }
        return entry.getValue();
    }

    private static long hash(String value) {
        try {
            byte[] digest = MessageDigest.getInstance("MD5")
                    .digest(value.getBytes(StandardCharsets.UTF_8));
            long result = 0;
            for (int i = 0; i < 8; i++) {
                result = (result << 8) | (digest[i] & 0xff);
            }
            return result;
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("MD5 not available", e);
        }
    }
}
