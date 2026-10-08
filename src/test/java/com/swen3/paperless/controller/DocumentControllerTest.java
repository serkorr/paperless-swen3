package com.swen3.paperless.controller;

import com.swen3.paperless.dto.DocumentRequest;
import com.swen3.paperless.dto.DocumentResponse;
import com.swen3.paperless.entity.DocumentStatus;
import com.swen3.paperless.service.DocumentService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class DocumentControllerTest {

    private DocumentService documentService;
    private DocumentController documentController;

    @BeforeEach
    void setUp() {
        documentService = mock(DocumentService.class);
        documentController = new DocumentController(documentService);
    }

    // Creates a reusable sample response for controller tests
    private DocumentResponse createResponse(Long id) {
        return new DocumentResponse(
                id,
                "SWEN3 Notes",
                "notes.pdf",
                "application/pdf",
                LocalDateTime.of(2026, 10, 8, 12, 0),
                DocumentStatus.UPLOADED,
                null
        );
    }

    // Verifies that creating a document returns HTTP 201 and its location
    @Test
    void shouldCreateDocument() {
        DocumentRequest request = new DocumentRequest(
                "SWEN3 Notes",
                "notes.pdf",
                "application/pdf"
        );

        DocumentResponse expected = createResponse(1L);

        when(documentService.createDocument(request))
                .thenReturn(expected);

        ResponseEntity<DocumentResponse> response =
                documentController.createDocument(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expected, response.getBody());
        assertEquals(
                "/api/documents/1",
                response.getHeaders().getLocation().toString()
        );

        verify(documentService).createDocument(request);
    }

    // Verifies that all documents are returned from the service
    @Test
    void shouldGetAllDocuments() {
        List<DocumentResponse> expected = List.of(
                createResponse(1L),
                createResponse(2L)
        );

        when(documentService.getAllDocuments())
                .thenReturn(expected);

        List<DocumentResponse> response =
                documentController.getAllDocuments();

        assertEquals(2, response.size());
        assertEquals(expected, response);

        verify(documentService).getAllDocuments();
    }

    // Verifies that an empty list is returned when no documents exist
    @Test
    void shouldReturnEmptyDocumentList() {
        when(documentService.getAllDocuments())
                .thenReturn(List.of());

        List<DocumentResponse> response =
                documentController.getAllDocuments();

        assertTrue(response.isEmpty());
        verify(documentService).getAllDocuments();
    }

    // Verifies that a document can be retrieved by its ID
    @Test
    void shouldGetDocumentById() {
        DocumentResponse expected = createResponse(1L);

        when(documentService.getDocumentById(1L))
                .thenReturn(expected);

        DocumentResponse response =
                documentController.getDocumentById(1L);

        assertEquals(expected, response);
        verify(documentService).getDocumentById(1L);
    }

    // Verifies that updated document data is returned correctly
    @Test
    void shouldUpdateDocument() {
        DocumentRequest request = new DocumentRequest(
                "Updated Notes",
                "updated.pdf",
                "application/pdf"
        );

        DocumentResponse expected = new DocumentResponse(
                1L,
                "Updated Notes",
                "updated.pdf",
                "application/pdf",
                LocalDateTime.of(2026, 10, 8, 12, 0),
                DocumentStatus.UPLOADED,
                null
        );

        when(documentService.updateDocument(1L, request))
                .thenReturn(expected);

        DocumentResponse response =
                documentController.updateDocument(1L, request);

        assertEquals("Updated Notes", response.title());
        assertEquals("updated.pdf", response.filename());
        assertNull(response.categoryId());

        verify(documentService).updateDocument(1L, request);
    }

    // Verifies that deleting a document calls the service
    @Test
    void shouldDeleteDocument() {
        documentController.deleteDocument(1L);

        verify(documentService).deleteDocument(1L);
    }

    // Verifies that a missing document produces an HTTP 404 response
    @Test
    void shouldReturnNotFoundForMissingDocument() {
        NoSuchElementException exception =
                new NoSuchElementException(
                        "Document not found with id: 999"
                );

        ResponseEntity<String> response =
                documentController.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(
                "Document not found with id: 999",
                response.getBody()
        );
    }

    // Verifies that assigning a category returns the updated document
    @Test
    void shouldAssignCategoryToDocument() {
        Long documentId = 1L;
        Long categoryId = 2L;

        DocumentResponse expected = new DocumentResponse(
                documentId,
                "SWEN3 Notes",
                "notes.pdf",
                "application/pdf",
                LocalDateTime.of(2026, 10, 8, 12, 0),
                DocumentStatus.UPLOADED,
                categoryId
        );

        when(documentService.assignCategory(documentId, categoryId))
                .thenReturn(expected);

        DocumentResponse response =
                documentController.assignCategory(documentId, categoryId);

        assertEquals(documentId, response.id());
        assertEquals(categoryId, response.categoryId());

        verify(documentService).assignCategory(documentId, categoryId);
    }

    // Verifies that a missing category produces an HTTP 404 response
    @Test
    void shouldReturnNotFoundForMissingCategory() {
        NoSuchElementException exception =
                new NoSuchElementException(
                        "Category not found with id: 999"
                );

        ResponseEntity<String> response =
                documentController.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(
                "Category not found with id: 999",
                response.getBody()
        );
    }
}