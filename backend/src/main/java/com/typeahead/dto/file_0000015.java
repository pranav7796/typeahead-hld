package com.typeahead.dto;

import java.util.List;

/** API response for a prefix lookup. */
public record SuggestionResponse(String prefix, List<Suggestion> suggestions) {
}
