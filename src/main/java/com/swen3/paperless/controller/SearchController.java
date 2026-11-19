package com.swen3.paperless.controller;

import com.swen3.paperless.search.SearchResponse;
import com.swen3.paperless.search.SearchService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/search")
public class SearchController {

    private final SearchService searchService;

    public SearchController(SearchService searchService) {
        this.searchService = searchService;
    }

    // Search documents by OCR text.
    @GetMapping
    public List<SearchResponse> search(
            @RequestParam String query) {

        return searchService.search(query);
    }
}
