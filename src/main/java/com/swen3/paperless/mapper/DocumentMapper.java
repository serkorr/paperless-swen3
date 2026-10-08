package com.swen3.paperless.mapper;

import com.swen3.paperless.dto.DocumentRequest;
import com.swen3.paperless.dto.DocumentResponse;
import com.swen3.paperless.entity.Document;

public final class DocumentMapper {

    // Prevent instantiation of this utility class
    private DocumentMapper() {
    }

    // Convert the request DTO into a Document entity
    public static Document toEntity(DocumentRequest request) {
        return new Document(
                request.title(),
                request.filename(),
                request.contentType()
        );
    }

    // Convert the Document entity into a response DTO, including its category ID
    public static DocumentResponse toResponse(Document document) {
        Long categoryId = document.getCategory() != null
                ? document.getCategory().getId()
                : null;

        return new DocumentResponse(
                document.getId(),
                document.getTitle(),
                document.getFilename(),
                document.getContentType(),
                document.getUploadedAt(),
                document.getStatus(),
                categoryId
        );
    }
}