package com.swen3.indexingworker.listener;

import com.swen3.indexingworker.model.IndexedDocument;
import com.swen3.indexingworker.repository.IndexedDocumentRepository;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class IndexingMessageListener {

    private final IndexedDocumentRepository repository;

    public IndexingMessageListener(IndexedDocumentRepository repository) {
        this.repository = repository;
    }

    // Receive OCR results and store them in Elasticsearch.
    @RabbitListener(queues = "paperless.indexing.queue")
    public void processOcrResult(String message) {
        String[] parts = message.split(":", 2);

        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid OCR result message");
        }

        Long documentId = Long.valueOf(parts[0]);
        String extractedText = parts[1];

        repository.save(new IndexedDocument(documentId, extractedText));
    }
}