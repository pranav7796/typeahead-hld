package com.typeahead.service;

import com.typeahead.cache.PrefixSuggestionCache;
import com.typeahead.configuration.Constants;
import com.typeahead.model.SearchEvent;
import com.typeahead.repository.QueryFrequencyRepository;
import com.typeahead.repository.SearchEventRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Drains the search buffer and writes it to PostgreSQL in one batch: increments
 * query counts, stores raw events, then invalidates the affected prefix caches
 * so the next read repopulates from fresh data.
 */
@Service
public class BatchAggregationService {

    private static final Logger log = LoggerFactory.getLogger(BatchAggregationService.class);

    private final SearchEventBuffer searchEventBuffer;
    private final QueryFrequencyRepository queryFrequencyRepository;
    private final SearchEventRepository searchEventRepository;
    private final PrefixSuggestionCache suggestionCache;

    public BatchAggregationService(SearchEventBuffer searchEventBuffer,
                                   QueryFrequencyRepository queryFrequencyRepository,
                                   SearchEventRepository searchEventRepository,
                                   PrefixSuggestionCache suggestionCache) {
        this.searchEventBuffer = searchEventBuffer;
        this.queryFrequencyRepository = queryFrequencyRepository;
        this.searchEventRepository = searchEventRepository;
        this.suggestionCache = suggestionCache;
    }

    @Transactional
    public void flushBufferedCounts() {
        List<String> submissions = searchEventBuffer.flushBufferedCounts();
        if (submissions.isEmpty()) {
            return;
        }

        Map<String, Long> countsByQuery = submissions.stream()
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        countsByQuery.forEach(queryFrequencyRepository::incrementSearchCount);
        persistRawEvents(submissions);
        invalidateAffectedPrefixes(countsByQuery.keySet());

        log.info("Flushed {} submissions across {} distinct queries", submissions.size(), countsByQuery.size());
    }

    private void persistRawEvents(List<String> submissions) {
        Instant now = Instant.now();
        List<SearchEvent> events = submissions.stream()
                .map(query -> new SearchEvent(query, now))
                .toList();
        searchEventRepository.saveAll(events);
    }

    private void invalidateAffectedPrefixes(Set<String> queries) {
        Set<String> prefixes = new HashSet<>();
        for (String query : queries) {
            int maxLength = Math.min(query.length(), Constants.MAX_PREFIX_LENGTH);
            for (int length = 1; length <= maxLength; length++) {
                prefixes.add(query.substring(0, length));
            }
        }
        prefixes.forEach(suggestionCache::invalidatePrefix);
    }
}
