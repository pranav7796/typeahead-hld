package com.typeahead.dto;

/** Response for POST /api/search. The submission is buffered, not written synchronously. */
public record SearchSubmissionResponse(boolean accepted) {
}
