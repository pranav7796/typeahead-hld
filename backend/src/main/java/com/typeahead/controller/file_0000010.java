package com.typeahead.controller;

import com.typeahead.dto.SearchSubmissionRequest;
import com.typeahead.dto.SearchSubmissionResponse;
import com.typeahead.service.SearchSubmissionService;
import jakarta.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchSubmissionService searchSubmissionService;

    public SearchController(SearchSubmissionService searchSubmissionService) {
        this.searchSubmissionService = searchSubmissionService;
    }

    @PostMapping
    public SearchSubmissionResponse submitSearch(@Valid @RequestBody SearchSubmissionRequest request) {
        boolean accepted = searchSubmissionService.submitSearch(request.query());
        return new SearchSubmissionResponse(accepted);
    }
}
