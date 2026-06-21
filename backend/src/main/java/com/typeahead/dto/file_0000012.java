package com.typeahead.dto;

import jakarta.validation.constraints.NotBlank;

/** Body of POST /api/search. */
public record SearchSubmissionRequest(@NotBlank String query) {
}
