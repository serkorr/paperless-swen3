package com.swen3.ocrworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;
import org.springframework.amqp.rabbit.core.RabbitTemplate;

import java.io.IOException;

@Component
public class OcrMessageListener {

    private static final Logger logger =
            LoggerFactory.getLogger(OcrMessageListener.class);

    private final DocumentApiClient documentApiClient;
    private final MinioStorageService minioStorageService;
    private final OcrService ocrService;
    private final RabbitTemplate rabbitTemplate;

    // Inject the services needed to process OCR requests.
    public OcrMessageListener(
            DocumentApiClient documentApiClient,
            MinioStorageService minioStorageService,
            OcrService ocrService,
            RabbitTemplate rabbitTemplate) {

        this.documentApiClient = documentApiClient;
        this.minioStorageService = minioStorageService;
        this.ocrService = ocrService;
        this.rabbitTemplate = rabbitTemplate;
    }

    // Receive an OCR request and download its PDF from MinIO.
    @RabbitListener(queues = "paperless.ocr.queue")
    public void processDocument(String documentId) throws IOException {
        logger.info("OCR worker received document ID: {}", documentId);

        // Retrieve the PDF object name from the backend.
        Long id = Long.valueOf(documentId);
        String storedFilename = documentApiClient.getStoredFilename(id);

        // Download the PDF from MinIO.
        byte[] pdfBytes = minioStorageService.downloadPdf(storedFilename);

        logger.info(
                "Downloaded PDF from MinIO: {}, size: {} bytes",
                storedFilename,
                pdfBytes.length
        );
        // Extract text from the downloaded PDF using OCR.
        String extractedText;
        try {
            extractedText = ocrService.extractTextFromPdf(pdfBytes);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new IOException("OCR processing was interrupted", e);
        }

        // Log the extracted text to verify OCR processing.
        logger.info(
                "OCR completed for document ID: {}, extracted text: {}",
                documentId,
                extractedText
        );

        // Send the extracted text to the indexing worker.
        String indexingMessage = documentId + ":" + extractedText;

        rabbitTemplate.convertAndSend(
                "",
                "paperless.indexing.queue",
                indexingMessage
        );

        logger.info(
                "OCR result sent for indexing, document ID: {}",
                documentId
        );
    }
}
