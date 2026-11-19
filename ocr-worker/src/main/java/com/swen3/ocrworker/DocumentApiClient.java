
package com.swen3.ocrworker;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestClient;

@Service
public class DocumentApiClient {

    private final RestClient restClient;

    // Configure the HTTP client with the backend API address.
    public DocumentApiClient(
            @Value("${paperless.backend.url}") String backendUrl) {

        this.restClient = RestClient.builder()
                .baseUrl(backendUrl)
                .build();
    }

    // Retrieve the MinIO object name for the given document ID.
    public String getStoredFilename(Long documentId) {
        DocumentMetadata document = restClient.get()
                .uri("/api/documents/{id}", documentId)
                .retrieve()
                .body(DocumentMetadata.class);

        if (document == null || document.storedFilename() == null
                || document.storedFilename().isBlank()) {
            throw new IllegalStateException(
                    "Stored filename not found for document: " + documentId
            );
        }

        return document.storedFilename();
    }

    // Map only the JSON field needed by the OCR worker.
    private record DocumentMetadata(String storedFilename) {
    }
}
