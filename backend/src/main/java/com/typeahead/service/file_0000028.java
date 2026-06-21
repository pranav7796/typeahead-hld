package com.typeahead.service;

import com.typeahead.cache.PrefixSuggestionCache;
import com.typeahead.dto.Suggestion;
import com.typeahead.dto.SuggestionResponse;
import com.typeahead.model.QueryFrequency;
import com.typeahead.repository.QueryFrequencyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Read path: cache-aside lookup of suggestions for a prefix.
 * Cache hit -> return; miss -> query PostgreSQL, populate cache, return.
 */
@Service
public class SuggestionService {

    private final PrefixSuggestionCache suggestionCache;
    private final QueryFrequencyRepository queryFrequencyRepository;

    public SuggestionService(PrefixSuggestionCache suggestionCache,
                             QueryFrequencyRepository queryFrequencyRepository) {
        this.suggestionCache = suggestionCache;
        this.queryFrequencyRepository = queryFrequencyRepository;
    }

    public SuggestionResponse getSuggestionsForPrefix(String prefix) {
        String normalizedPrefix = normalize(prefix);
        if (normalizedPrefix.isEmpty()) {
            return new SuggestionResponse(prefix, List.of());
        }

        Optional<List<Suggestion>> cached = suggestionCache.get(normalizedPrefix);
        if (cached.isPresent()) {
            return new SuggestionResponse(prefix, cached.get());
        }

        List<Suggestion> suggestions = loadFromDatabase(normalizedPrefix);
        suggestionCache.populate(normalizedPrefix, suggestions);
        return new SuggestionResponse(prefix, suggestions);
    }

    private List<Suggestion> loadFromDatabase(String normalizedPrefix) {
        List<QueryFrequency> rows = queryFrequencyRepository
                .findTop10ByQueryTextStartingWithOrderByTotalCountDesc(normalizedPrefix);
        return rows.stream()
                .map(row -> new Suggestion(row.getQueryText(), row.getTotalCount()))
                .toList();
    }

    private static String normalize(String prefix) {
        return prefix == null ? "" : prefix.trim().toLowerCase();
    }
}
