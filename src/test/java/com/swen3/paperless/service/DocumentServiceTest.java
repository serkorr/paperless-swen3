package com.swen3.paperless.service;

import com.swen3.paperless.dto.DocumentRequest;
import com.swen3.paperless.dto.DocumentResponse;
import com.swen3.paperless.entity.Category;
import com.swen3.paperless.entity.Document;
import com.swen3.paperless.entity.DocumentStatus;
import com.swen3.paperless.repository.CategoryRepository;
import com.swen3.paperless.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

class DocumentServiceTest {

    @Mock
    private DocumentRepository documentRepository;

    @Mock
    private CategoryRepository categoryRepository;

    private DocumentService documentService;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        documentService = new DocumentService(
                documentRepository,
                categoryRepository
        );
    }

    // Verifies that a new document is saved with the expected properties
    @Test
    void shouldCreateDocument() {
        DocumentRequest request = new DocumentRequest(
                "SWEN3 Notes",
                "notes.pdf",
                "application/pdf"
        );

        Document savedDocument = new Document(
                "SWEN3 Notes",
                "notes.pdf",
                "application/pdf"
        );

        when(documentRepository.save(any(Document.class)))
                .thenReturn(savedDocument);

        DocumentResponse response =
                documentService.createDocument(request);

        assertEquals("SWEN3 Notes", response.title());
        assertEquals("notes.pdf", response.filename());
        assertEquals("application/pdf", response.contentType());
        assertEquals(DocumentStatus.UPLOADED, response.status());
        assertNotNull(response.uploadedAt());
        assertNull(response.categoryId());

        verify(documentRepository).save(any(Document.class));
    }

    // Verifies that an existing document can be retrieved by its ID
    @Test
    void shouldGetDocumentById() {
        Long documentId = 1L;

        Document document = new Document(
                "SWEN3 Notes",
                "notes.pdf",
                "application/pdf"
        );

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        DocumentResponse response =
                documentService.getDocumentById(documentId);

        assertEquals("SWEN3 Notes", response.title());
        assertEquals("notes.pdf", response.filename());
        assertEquals("application/pdf", response.contentType());
        assertEquals(DocumentStatus.UPLOADED, response.status());
        assertNull(response.categoryId());

        verify(documentRepository).findById(documentId);
    }

    // Verifies that all stored documents are returned as a list
    @Test
    void shouldGetAllDocuments() {
        Document firstDocument = new Document(
                "First Document",
                "first.pdf",
                "application/pdf"
        );

        Document secondDocument = new Document(
                "Second Document",
                "second.pdf",
                "application/pdf"
        );

        when(documentRepository.findAll())
                .thenReturn(List.of(firstDocument, secondDocument));

        List<DocumentResponse> responses =
                documentService.getAllDocuments();

        assertEquals(2, responses.size());
        assertEquals("First Document", responses.get(0).title());
        assertEquals("Second Document", responses.get(1).title());

        verify(documentRepository).findAll();
    }

    // Verifies that an empty list is returned when no documents exist
    @Test
    void shouldReturnEmptyListWhenNoDocumentsExist() {
        when(documentRepository.findAll())
                .thenReturn(List.of());

        List<DocumentResponse> responses =
                documentService.getAllDocuments();

        assertTrue(responses.isEmpty());
        verify(documentRepository).findAll();
    }

    // Verifies that document details can be updated successfully
    @Test
    void shouldUpdateDocument() {
        Long documentId = 1L;

        Document existingDocument = new Document(
                "Old Title",
                "old.pdf",
                "application/pdf"
        );

        DocumentRequest request = new DocumentRequest(
                "Updated Title",
                "updated.pdf",
                "application/pdf"
        );

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.of(existingDocument));

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponse response =
                documentService.updateDocument(documentId, request);

        assertEquals("Updated Title", response.title());
        assertEquals("updated.pdf", response.filename());
        assertEquals("application/pdf", response.contentType());
        assertEquals(DocumentStatus.UPLOADED, response.status());

        verify(documentRepository).findById(documentId);
        verify(documentRepository).save(existingDocument);
    }

    // Verifies that updating a document preserves its upload timestamp
    @Test
    void shouldPreserveUploadedAtWhenUpdatingDocument() {
        Long documentId = 1L;

        Document existingDocument = new Document(
                "Original Title",
                "original.pdf",
                "application/pdf"
        );

        var originalUploadedAt = existingDocument.getUploadedAt();

        DocumentRequest request = new DocumentRequest(
                "New Title",
                "new.pdf",
                "application/pdf"
        );

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.of(existingDocument));

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponse response =
                documentService.updateDocument(documentId, request);

        assertEquals(originalUploadedAt, response.uploadedAt());
        verify(documentRepository).save(existingDocument);
    }

    // Verifies that an existing document is deleted through the repository
    @Test
    void shouldDeleteDocument() {
        Long documentId = 1L;

        Document document = new Document(
                "SWEN3 Notes",
                "notes.pdf",
                "application/pdf"
        );

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        documentService.deleteDocument(documentId);

        verify(documentRepository).findById(documentId);
        verify(documentRepository).delete(document);
    }

    // Verifies that retrieving a missing document throws an exception
    @Test
    void shouldThrowExceptionWhenDocumentNotFound() {
        Long documentId = 999L;

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> documentService.getDocumentById(documentId)
        );

        assertEquals(
                "Document not found with id: 999",
                exception.getMessage()
        );

        verify(documentRepository).findById(documentId);
    }

    // Verifies that updating a missing document does not save anything
    @Test
    void shouldThrowExceptionWhenUpdatingMissingDocument() {
        Long documentId = 999L;

        DocumentRequest request = new DocumentRequest(
                "Updated Title",
                "updated.pdf",
                "application/pdf"
        );

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> documentService.updateDocument(documentId, request)
        );

        verify(documentRepository).findById(documentId);
        verify(documentRepository, never())
                .save(any(Document.class));
    }

    // Verifies that deleting a missing document does not delete anything
    @Test
    void shouldThrowExceptionWhenDeletingMissingDocument() {
        Long documentId = 999L;

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> documentService.deleteDocument(documentId)
        );

        verify(documentRepository).findById(documentId);
        verify(documentRepository, never())
                .delete(any(Document.class));
    }

    // Verifies that an existing category can be assigned to a document
    @Test
    void shouldAssignCategoryToDocument() {
        Long documentId = 1L;
        Long categoryId = 2L;

        Document document = new Document(
                "SWEN3 Notes",
                "notes.pdf",
                "application/pdf"
        );

        Category category = mock(Category.class);
        when(category.getId()).thenReturn(categoryId);

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.of(category));

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentResponse response =
                documentService.assignCategory(documentId, categoryId);

        assertSame(category, document.getCategory());
        assertEquals(categoryId, response.categoryId());

        verify(documentRepository).findById(documentId);
        verify(categoryRepository).findById(categoryId);
        verify(documentRepository).save(document);
    }

    // Verifies that assigning a category fails when the document is missing
    @Test
    void shouldThrowExceptionWhenAssigningCategoryToMissingDocument() {
        Long documentId = 999L;
        Long categoryId = 2L;

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> documentService.assignCategory(documentId, categoryId)
        );

        assertEquals(
                "Document not found with id: 999",
                exception.getMessage()
        );

        verify(categoryRepository, never()).findById(any());
        verify(documentRepository, never())
                .save(any(Document.class));
    }

    // Verifies that assigning a missing category does not save the document
    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        Long documentId = 1L;
        Long categoryId = 999L;

        Document document = new Document(
                "SWEN3 Notes",
                "notes.pdf",
                "application/pdf"
        );

        when(documentRepository.findById(documentId))
                .thenReturn(Optional.of(document));

        when(categoryRepository.findById(categoryId))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> documentService.assignCategory(documentId, categoryId)
        );

        assertEquals(
                "Category not found with id: 999",
                exception.getMessage()
        );

        assertNull(document.getCategory());

        verify(documentRepository, never())
                .save(any(Document.class));
    }
}