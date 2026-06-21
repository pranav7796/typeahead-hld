package com.typeahead.hashing;

/** A single Redis cache node on the hash ring. */
public record CacheNode(String id, String host, int port) {
}
