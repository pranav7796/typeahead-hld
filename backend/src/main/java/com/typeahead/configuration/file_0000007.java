package com.typeahead.configuration;

import java.time.Duration;

/**
 * Centralized constants. Redis keys and cache settings live here only —
 * never hardcode them elsewhere.
 */
public final class Constants {

    private Constants() {
    }

    /** Redis key prefix for cached suggestions, e.g. "suggest:iph". */
    public static final String SUGGEST_KEY_PREFIX = "suggest:";

    /** How long a populated suggestion entry stays cached. */
    public static final Duration SUGGESTION_TTL = Duration.ofMinutes(5);

    /** Max number of suggestions returned for a prefix. */
    public static final int MAX_SUGGESTIONS = 10;

    /**
     * Longest prefix we invalidate after a batch write. Typeahead lookups use
     * short prefixes, so invalidating prefixes of length 1..MAX keeps the work bounded.
     */
    public static final int MAX_PREFIX_LENGTH = 6;

    public static String suggestionKey(String prefix) {
        return SUGGEST_KEY_PREFIX + prefix;
    }
}
