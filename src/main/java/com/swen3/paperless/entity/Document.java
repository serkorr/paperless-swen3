package com.swen3.paperless.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "documents")
public class Document {

    // Unique identifier for each document
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // Title of the document
    @Column(nullable = false)
    private String title;

    // Original filename of the uploaded document
    @Column(nullable = false)
    private String filename;

    // MIME type of the document (e.g. application/pdf)
    private String contentType;

    // Timestamp when the document was created
    @Column(nullable = false, updatable = false)
    private LocalDateTime uploadedAt;

    // Current processing status of the document
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentStatus status;

    // Each document can belong to one category
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    // Default constructor required by JPA
    protected Document() {
    }

    // Constructor for creating a new document
    public Document(String title, String filename,
                    String contentType) {
        this.title = title;
        this.filename = filename;
        this.contentType = contentType;
        this.uploadedAt = LocalDateTime.now();
        this.status = DocumentStatus.UPLOADED;
    }

    // Get the unique document ID
    public Long getId() {
        return id;
    }

    // Get the document title
    public String getTitle() {
        return title;
    }

    // Update the document title
    public void setTitle(String title) {
        this.title = title;
    }

    // Get the original filename
    public String getFilename() {
        return filename;
    }

    // Update the original filename
    public void setFilename(String filename) {
        this.filename = filename;
    }

    // Get the MIME type
    public String getContentType() {
        return contentType;
    }

    // Update the MIME type
    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    // Get the document creation timestamp
    public LocalDateTime getUploadedAt() {
        return uploadedAt;
    }

    // Get the current processing status
    public DocumentStatus getStatus() {
        return status;
    }

    // Update the processing status
    public void setStatus(DocumentStatus status) {
        this.status = status;
    }

    // Get the category assigned to the document
    public Category getCategory() {
        return category;
    }

    // Assign or update the document category
    public void setCategory(Category category) {
        this.category = category;
    }
}