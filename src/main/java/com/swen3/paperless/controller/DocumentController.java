package com.swen3.paperless.controller;

import com.swen3.paperless.dto.DocumentRequest;
import com.swen3.paperless.dto.DocumentResponse;
import com.swen3.paperless.service.DocumentService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.*;

import java.io.IOException;
import java.net.URI;
import java.util.List;
import java.util.NoSuchElementException;

@RestController
@RequestMapping("/api/documents")
public class DocumentController {

    private final DocumentService documentService;

    // Inject the service through the constructor
    public DocumentController(DocumentService documentService) {
        this.documentService = documentService;
    }

    // Create a new document
    @PostMapping
    public ResponseEntity<DocumentResponse> createDocument(
            @Valid @RequestBody DocumentRequest request) {

        DocumentResponse response = documentService.createDocument(request);

        return ResponseEntity
                .created(URI.create("/api/documents/" + response.id()))
                .body(response);
    }

    // Upload a PDF file and create its document metadata.
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<DocumentResponse> uploadDocument(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title) throws IOException {

        DocumentResponse response = documentService.uploadDocument(file, title);

        return ResponseEntity
                .created(URI.create("/api/documents/" + response.id()))
                .body(response);
    }

    // Retrieve all documents
    @GetMapping
    public List<DocumentResponse> getAllDocuments() {
        return documentService.getAllDocuments();
    }

    // Retrieve a document by its ID
    @GetMapping("/{id}")
    public DocumentResponse getDocumentById(@PathVariable Long id) {
        return documentService.getDocumentById(id);
    }

    // Update an existing document
    @PutMapping("/{id}")
    public DocumentResponse updateDocument(
            @PathVariable Long id,
            @Valid @RequestBody DocumentRequest request) {

        return documentService.updateDocument(id, request);
    }

    // Assign an existing category to a document
    @PutMapping("/{documentId}/category/{categoryId}")
    public DocumentResponse assignCategory(
            @PathVariable Long documentId,
            @PathVariable Long categoryId) {

        return documentService.assignCategory(documentId, categoryId);
    }

    // Delete an existing document
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteDocument(@PathVariable Long id) {
        documentService.deleteDocument(id);
    }

    // Return HTTP 400 for invalid PDF uploads or document titles.
    @ExceptionHandler(IllegalArgumentException.class)
    public ResponseEntity<String> handleBadRequest(
            IllegalArgumentException exception) {

        return ResponseEntity
                .status(HttpStatus.BAD_REQUEST)
                .body(exception.getMessage());
    }

    // Return HTTP 500 when a PDF cannot be read or saved.
    @ExceptionHandler(IOException.class)
    public ResponseEntity<String> handleFileError() {

        return ResponseEntity
                .status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body("PDF file could not be processed");
    }

    // Return HTTP 404 when a document or category does not exist
    @ExceptionHandler(NoSuchElementException.class)
    public ResponseEntity<String> handleNotFound(
            NoSuchElementException exception) {

        return ResponseEntity
                .status(HttpStatus.NOT_FOUND)
                .body(exception.getMessage());
    }
}