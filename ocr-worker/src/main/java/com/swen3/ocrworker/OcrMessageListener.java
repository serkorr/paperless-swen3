package com.swen3.ocrworker;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
public class OcrMessageListener {

    private static final Logger logger =
            LoggerFactory.getLogger(OcrMessageListener.class);

    // Receive OCR requests and log them until OCR processing is implemented.
    @RabbitListener(queues = "paperless.ocr.queue")
    public void processDocument(String documentId) {
        logger.info("OCR worker received document ID: {}", documentId);
    }
}