package com.swen3.paperless.search;

import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;

import java.util.List;

public interface IndexedDocumentRepository
        extends ElasticsearchRepository<IndexedDocument, Long> {

    // Find documents containing the specified OCR search term.
    List<IndexedDocument> findByExtractedTextContaining(String text);
}
