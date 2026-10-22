package com.swen3.paperless.service;

import com.swen3.paperless.dto.DocumentRequest;
import com.swen3.paperless.dto.DocumentResponse;
import com.swen3.paperless.entity.Category;
import com.swen3.paperless.entity.Document;
import com.swen3.paperless.mapper.DocumentMapper;
import com.swen3.paperless.messaging.OcrMessagePublisher;
import com.swen3.paperless.repository.CategoryRepository;
import com.swen3.paperless.repository.DocumentRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    private static final Logger logger =
            LoggerFactory.getLogger(DocumentService.class);

    private final DocumentRepository documentRepository;
    private final CategoryRepository categoryRepository;
    private final OcrMessagePublisher ocrMessagePublisher;

    @Value("${paperless.upload.directory}")
    private String uploadDirectory;

    // Constructor injection makes dependencies explicit and testable.
    public DocumentService(
            DocumentRepository documentRepository,
            CategoryRepository categoryRepository,
            OcrMessagePublisher ocrMessagePublisher) {

        this.documentRepository = documentRepository;
        this.categoryRepository = categoryRepository;
        this.ocrMessagePublisher = ocrMessagePublisher;
    }

    // Create a document and publish an OCR request after the transaction commits.
    @Transactional
    public DocumentResponse createDocument(DocumentRequest request) {
        Document document = DocumentMapper.toEntity(request);
        Document savedDocument = documentRepository.save(document);

        Long documentId = savedDocument.getId();

        // Publish only after the database transaction commits successfully.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        ocrMessagePublisher.publish(documentId);
                    }
                }
        );

        return DocumentMapper.toResponse(savedDocument);
    }

    // Validate and store an uploaded PDF, then create its document metadata.
    @Transactional
    public DocumentResponse uploadDocument(MultipartFile file, String title)
            throws IOException {

        validatePdfFile(file);

        if (title == null || title.isBlank()) {
            throw new IllegalArgumentException("Document title must not be empty");
        }

        // Generate a unique filename for storing the uploaded PDF.
        String storedFilename = UUID.randomUUID() + ".pdf";

        savePdfFile(file, storedFilename);


        // Remove the uploaded file if the database transaction fails.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCompletion(int status) {
                        if (status != TransactionSynchronization.STATUS_COMMITTED) {
                            try {
                                deletePdfFile(storedFilename);
                            } catch (IOException e) {
                                logger.error(
                                        "Failed to delete PDF after transaction rollback: {}",
                                        storedFilename,
                                        e
                                );
                            }
                        }
                    }
                }
        );

        // Create metadata for the uploaded PDF.
        Document document = new Document(
                title,
                file.getOriginalFilename(),
                "application/pdf"
        );

        // Link the document to its unique stored filename.
        document.setStoredFilename(storedFilename);

        // Persist the document with its internal storage filename.
        Document savedDocument = documentRepository.save(document);

        Long documentId = savedDocument.getId();

        // Publish the OCR request after the database transaction commits.
        TransactionSynchronizationManager.registerSynchronization(
                new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        ocrMessagePublisher.publish(documentId);
                    }
                }
        );

        return DocumentMapper.toResponse(savedDocument);
    }

    // Return all documents as response DTOs.
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(DocumentMapper::toResponse)
                .toList();
    }

    // Find a document by its unique identifier.
    public DocumentResponse getDocumentById(Long id) {
        Document document = findDocumentOrThrow(id);
        return DocumentMapper.toResponse(document);
    }

    // Update metadata without changing the ID or upload timestamp.
    @Transactional
    public DocumentResponse updateDocument(Long id, DocumentRequest request) {
        Document document = findDocumentOrThrow(id);

        document.setTitle(request.title());
        document.setFilename(request.filename());
        document.setContentType(request.contentType());

        Document updatedDocument = documentRepository.save(document);
        return DocumentMapper.toResponse(updatedDocument);
    }

    // Delete a document after checking that it exists.
    @Transactional
    public void deleteDocument(Long id) {
        Document document = findDocumentOrThrow(id);
        documentRepository.delete(document);
    }

    // Assign an existing category to a document.
    @Transactional
    public DocumentResponse assignCategory(Long documentId, Long categoryId) {
        Document document = findDocumentOrThrow(documentId);

        Category category = categoryRepository.findById(categoryId)
                .orElseThrow(() -> new NoSuchElementException(
                        "Category not found with id: " + categoryId
                ));

        document.setCategory(category);

        Document updatedDocument = documentRepository.save(document);
        return DocumentMapper.toResponse(updatedDocument);
    }

    // Validate the uploaded file type and PDF signature.
    private void validatePdfFile(MultipartFile file) throws IOException {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("PDF file must not be empty");
        }

        if (!"application/pdf".equals(file.getContentType())) {
            throw new IllegalArgumentException("Only PDF files are allowed");
        }

        // Check the PDF signature instead of trusting only the filename.
        byte[] signature = new byte[5];

        try (InputStream inputStream = file.getInputStream()) {
            if (inputStream.readNBytes(signature, 0, 5) != 5
                    || signature[0] != '%'
                    || signature[1] != 'P'
                    || signature[2] != 'D'
                    || signature[3] != 'F'
                    || signature[4] != '-') {
                throw new IllegalArgumentException("Invalid PDF file");
            }
        }
    }

    // Save a PDF file using a generated name to avoid overwriting existing files.

    // Save a PDF file and clean up if the write fails.
    private void savePdfFile(MultipartFile file, String storedFilename)
            throws IOException {

        Path uploadPath = Path.of(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        Files.createDirectories(uploadPath);

        Path targetPath = uploadPath.resolve(storedFilename);

        try {
            file.transferTo(targetPath);
        } catch (IOException | RuntimeException e) {
            try {
                deletePdfFile(storedFilename);
            } catch (IOException cleanupError) {
                logger.error(
                        "Failed to clean up incomplete PDF: {}",
                        storedFilename,
                        cleanupError
                );
            }

            throw e;
        }
    }

    // Delete a stored PDF file if it is no longer needed.
    private void deletePdfFile(String storedFilename) throws IOException {
        Path uploadPath = Path.of(uploadDirectory)
                .toAbsolutePath()
                .normalize();

        Path targetPath = uploadPath.resolve(storedFilename);

        Files.deleteIfExists(targetPath);
    }

    // Centralize document lookup and missing-document handling.
    private Document findDocumentOrThrow(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Document not found with id: " + id
                ));
    }
}