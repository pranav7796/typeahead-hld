package com.typeahead.service;

import org.springframework.stereotype.Service;

/**
 * Write path entry point: validate/normalize a submitted query and hand it to
 * the in-memory buffer. The batch writer persists it later.
 */
@Service
public class SearchSubmissionService {

    private final SearchEventBuffer searchEventBuffer;

    public SearchSubmissionService(SearchEventBuffer searchEventBuffer) {
        this.searchEventBuffer = searchEventBuffer;
    }

    public boolean submitSearch(String query) {
        String normalized = query == null ? "" : query.trim().toLowerCase();
        if (normalized.isEmpty()) {
            return false;
        }
        searchEventBuffer.incrementSearchCount(normalized);
        return true;
    }
}
