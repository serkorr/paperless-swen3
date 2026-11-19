package com.swen3.paperless.search;

public record SearchResponse(
        Long documentId,
        String title,
        String filename,
        String extractedText
) {
}
