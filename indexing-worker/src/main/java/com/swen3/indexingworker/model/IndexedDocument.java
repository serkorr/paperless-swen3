
package com.swen3.indexingworker.model;

import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Document(indexName = "paperless-documents")
public class IndexedDocument {

    @Id
    private Long documentId;

    @Field(type = FieldType.Text)
    private String extractedText;

    public IndexedDocument() {
    }

    public IndexedDocument(Long documentId, String extractedText) {
        this.documentId = documentId;
        this.extractedText = extractedText;
    }

    public Long getDocumentId() {
        return documentId;
    }

    public void setDocumentId(Long documentId) {
        this.documentId = documentId;
    }

    public String getExtractedText() {
        return extractedText;
    }

    public void setExtractedText(String extractedText) {
        this.extractedText = extractedText;
    }
}