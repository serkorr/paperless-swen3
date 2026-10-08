
package com.swen3.paperless.dto;

import com.swen3.paperless.entity.DocumentStatus;
import java.time.LocalDateTime;

public record DocumentResponse(

        // Unique identifier of the document
        Long id,

        // Document metadata
        String title,
        String filename,
        String contentType,

        // Timestamp when the document was uploaded
        LocalDateTime uploadedAt,

        // Current processing status of the document
        DocumentStatus status,

        // ID of the assigned category, or null if none
        Long categoryId

) {
}