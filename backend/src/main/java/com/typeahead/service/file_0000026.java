package com.typeahead.service;

import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * In-memory buffer of submitted search queries. Submissions are appended here
 * cheaply and drained by the batch writer on a schedule, so no write touches
 * PostgreSQL on the request path.
 */
@Component
public class SearchEventBuffer {

    private final Queue<String> bufferedQueries = new ConcurrentLinkedQueue<>();

    public void incrementSearchCount(String queryText) {
        bufferedQueries.add(queryText);
    }

    /** Removes and returns everything buffered so far. */
    public List<String> flushBufferedCounts() {
        List<String> drained = new ArrayList<>();
        String query;
        while ((query = bufferedQueries.poll()) != null) {
            drained.add(query);
        }
        return drained;
    }
}
