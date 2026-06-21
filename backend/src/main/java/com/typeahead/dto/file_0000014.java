package com.typeahead.dto;

/** A single suggestion: the query text and its popularity count. */
public record Suggestion(String text, long count) {
}
