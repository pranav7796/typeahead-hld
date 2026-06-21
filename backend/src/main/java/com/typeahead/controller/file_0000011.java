package com.typeahead.controller;

import com.typeahead.dto.SuggestionResponse;
import com.typeahead.service.SuggestionService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/suggestions")
public class SuggestionController {

    private final SuggestionService suggestionService;

    public SuggestionController(SuggestionService suggestionService) {
        this.suggestionService = suggestionService;
    }

    @GetMapping
    public SuggestionResponse getSuggestions(@RequestParam(name = "prefix", defaultValue = "") String prefix) {
        return suggestionService.getSuggestionsForPrefix(prefix);
    }
}
