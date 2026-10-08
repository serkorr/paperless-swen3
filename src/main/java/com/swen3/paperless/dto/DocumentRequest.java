package com.swen3.paperless.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

public record DocumentRequest(

        // The document title must not be blank
        @NotBlank(message = "Document title is required")
        @Size(max = 255)
        String title,

        // The original filename
        @NotBlank(message = "Filename is required")
        @Size(max = 255)
        String filename,

        // The MIME type of the document
        @NotBlank(message = "Content type is required")
        String contentType

) {
}