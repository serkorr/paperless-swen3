package com.swen3.paperless.service;

import com.swen3.paperless.dto.DocumentRequest;
import com.swen3.paperless.dto.DocumentResponse;
import com.swen3.paperless.entity.Category;
import com.swen3.paperless.entity.Document;
import com.swen3.paperless.mapper.DocumentMapper;
import com.swen3.paperless.repository.CategoryRepository;
import com.swen3.paperless.repository.DocumentRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final CategoryRepository categoryRepository;

    // Constructor injection makes dependencies explicit and testable
    public DocumentService(
            DocumentRepository documentRepository,
            CategoryRepository categoryRepository) {

        this.documentRepository = documentRepository;
        this.categoryRepository = categoryRepository;
    }

    // Create and persist a new document
    @Transactional
    public DocumentResponse createDocument(DocumentRequest request) {
        Document document = DocumentMapper.toEntity(request);
        Document savedDocument = documentRepository.save(document);

        return DocumentMapper.toResponse(savedDocument);
    }

    // Return all documents as response DTOs
    public List<DocumentResponse> getAllDocuments() {
        return documentRepository.findAll()
                .stream()
                .map(DocumentMapper::toResponse)
                .toList();
    }

    // Find a document by its unique identifier
    public DocumentResponse getDocumentById(Long id) {
        Document document = findDocumentOrThrow(id);
        return DocumentMapper.toResponse(document);
    }

    // Update metadata without changing the ID or upload timestamp
    @Transactional
    public DocumentResponse updateDocument(Long id, DocumentRequest request) {
        Document document = findDocumentOrThrow(id);

        document.setTitle(request.title());
        document.setFilename(request.filename());
        document.setContentType(request.contentType());

        Document updatedDocument = documentRepository.save(document);
        return DocumentMapper.toResponse(updatedDocument);
    }

    // Delete a document after checking that it exists
    @Transactional
    public void deleteDocument(Long id) {
        Document document = findDocumentOrThrow(id);
        documentRepository.delete(document);
    }

    // Assign an existing category to a document
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

    // Centralize document lookup and missing-document handling
    private Document findDocumentOrThrow(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Document not found with id: " + id
                ));
    }
}