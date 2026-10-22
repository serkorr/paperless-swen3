package com.swen3.paperless.service;

import com.swen3.paperless.dto.DocumentRequest;
import com.swen3.paperless.dto.DocumentResponse;
import com.swen3.paperless.entity.Category;
import com.swen3.paperless.entity.Document;
import com.swen3.paperless.entity.DocumentStatus;
import com.swen3.paperless.messaging.OcrMessagePublisher;
import com.swen3.paperless.repository.CategoryRepository;
import com.swen3.paperless.repository.DocumentRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;


import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
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

    @Mock
    private OcrMessagePublisher ocrMessagePublisher;

    private DocumentService documentService;

    // Use a temporary directory for PDF upload tests.
    @TempDir
    Path tempDir;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        documentService = new DocumentService(
                documentRepository,
                categoryRepository,
                ocrMessagePublisher
        );

        // Configure a temporary upload directory for tests.
        ReflectionTestUtils.setField(documentService, "uploadDirectory", tempDir.toString());

    }

    // Verifies that a new document is saved with the expected properties
    @Test
    void shouldCreateDocument() {

        // Simulate an active transaction for the unit test.
        TransactionSynchronizationManager.initSynchronization();

        try {
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

            // Ensure no message is published before the transaction commits.
            verify(ocrMessagePublisher, never()).publish(any());

            // Simulate a successful transaction commit.
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(synchronization -> synchronization.afterCommit());

            // Verify that an OCR request is published after the commit.
            verify(ocrMessagePublisher).publish(savedDocument.getId());

        } finally {
            // Clean up transaction synchronization even if the test fails.
            TransactionSynchronizationManager.clearSynchronization();
        }
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


    // Verify that uploading a non-PDF file is rejected.
    @Test
    void shouldRejectNonPdfUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "notes.txt",
                "text/plain",
                "This is not a PDF".getBytes()
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> documentService.uploadDocument(file, "Test Document")
        );

        assertEquals("Only PDF files are allowed", exception.getMessage());

        // Invalid files must not be saved to the database or sent to OCR.
        verify(documentRepository, never()).save(any(Document.class));
        verify(ocrMessagePublisher, never()).publish(any());
    }

    // Verify that an empty PDF upload is rejected.
    @Test
    void shouldRejectEmptyPdfUpload() {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "empty.pdf",
                "application/pdf",
                new byte[0]
        );

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> documentService.uploadDocument(file, "Empty PDF")
        );

        assertEquals("PDF file must not be empty", exception.getMessage());

        verify(documentRepository, never()).save(any(Document.class));
        verify(ocrMessagePublisher, never()).publish(any());
    }


    // Verify that a valid PDF is saved and an OCR request is published after commit.
    @Test
    void shouldUploadValidPdf() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "test.pdf",
                "application/pdf",
                "%PDF-1.4\nTest PDF content".getBytes(StandardCharsets.UTF_8)
        );

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionSynchronizationManager.initSynchronization();

        try {
            DocumentResponse response =
                    documentService.uploadDocument(file, "Test PDF");

            assertEquals("Test PDF", response.title());
            assertEquals("test.pdf", response.filename());
            assertEquals("application/pdf", response.contentType());

            // Capture the document that was saved to the repository.
            org.mockito.ArgumentCaptor<Document> documentCaptor =
                    org.mockito.ArgumentCaptor.forClass(Document.class);

            verify(documentRepository).save(documentCaptor.capture());

            Document savedDocument = documentCaptor.getValue();

            // Verify that the PDF was saved with a unique filename.
            assertNotNull(savedDocument.getStoredFilename());
            assertTrue(savedDocument.getStoredFilename().endsWith(".pdf"));
            assertTrue(Files.exists(
                    tempDir.resolve(savedDocument.getStoredFilename())
            ));


            // No OCR message should be published before commit.
            verify(ocrMessagePublisher, never()).publish(any());

            // Simulate a successful database transaction.
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(synchronization -> synchronization.afterCommit());

            // Verify that the OCR request was published after commit.
            verify(ocrMessagePublisher).publish(savedDocument.getId());

        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    // Verify that an uploaded PDF is deleted after a transaction rollback.
    @Test
    void shouldDeleteUploadedPdfAfterRollback() throws Exception {
        MockMultipartFile file = new MockMultipartFile(
                "file",
                "rollback.pdf",
                "application/pdf",
                "%PDF-1.4\nRollback test".getBytes(StandardCharsets.UTF_8)
        );

        when(documentRepository.save(any(Document.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        TransactionSynchronizationManager.initSynchronization();

        try {
            documentService.uploadDocument(file, "Rollback Test");

            // Capture the stored document and its generated filename.
            org.mockito.ArgumentCaptor<Document> documentCaptor =
                    org.mockito.ArgumentCaptor.forClass(Document.class);

            verify(documentRepository).save(documentCaptor.capture());

            String storedFilename = documentCaptor.getValue().getStoredFilename();
            Path storedPath = tempDir.resolve(storedFilename);

            // The PDF exists before the transaction finishes.
            assertTrue(Files.exists(storedPath));

            // Simulate a failed database transaction.
            TransactionSynchronizationManager.getSynchronizations()
                    .forEach(synchronization -> synchronization.afterCompletion(
                            TransactionSynchronization.STATUS_ROLLED_BACK
                    ));

            // The PDF must be deleted after rollback.
            assertFalse(Files.exists(storedPath));

            // No OCR message should be sent for a failed transaction.
            verify(ocrMessagePublisher, never()).publish(any());

        } finally {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }
}