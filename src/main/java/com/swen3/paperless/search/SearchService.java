package com.swen3.paperless.search;

import com.swen3.paperless.entity.Document;
import com.swen3.paperless.repository.DocumentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SearchService {

    private final IndexedDocumentRepository indexedDocumentRepository;
    private final DocumentRepository documentRepository;

    public SearchService(
            IndexedDocumentRepository indexedDocumentRepository,
            DocumentRepository documentRepository) {

        this.indexedDocumentRepository = indexedDocumentRepository;
        this.documentRepository = documentRepository;
    }

    // Search OCR text and enrich results with document metadata.
    public List<SearchResponse> search(String keyword) {
        return indexedDocumentRepository
                .findByExtractedTextContaining(keyword)
                .stream()
                .map(indexedDocument -> {
                    Document document = documentRepository
                            .findById(indexedDocument.getDocumentId())
                            .orElse(null);

                    if (document == null) {
                        return null;
                    }

                    return new SearchResponse(
                            document.getId(),
                            document.getTitle(),
                            document.getFilename(),
                            indexedDocument.getExtractedText()
                    );
                })
                .filter(result -> result != null)
                .toList();
    }
}
