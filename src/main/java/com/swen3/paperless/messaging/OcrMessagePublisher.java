package com.swen3.paperless.messaging;

import com.swen3.paperless.config.RabbitMQConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
public class OcrMessagePublisher {

    private static final Logger logger =
            LoggerFactory.getLogger(OcrMessagePublisher.class);

    private final RabbitTemplate rabbitTemplate;

    public OcrMessagePublisher(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    // Publish a document ID to the OCR processing queue.
    public void publish(Long documentId) {
        rabbitTemplate.convertAndSend(RabbitMQConfig.OCR_QUEUE, documentId.toString());

        logger.info("OCR request published for document ID: {}", documentId);
    }
}