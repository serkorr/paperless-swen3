package com.swen3.paperless.config;

import org.springframework.amqp.core.Queue;
import org.springframework.amqp.rabbit.connection.ConnectionFactory;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMQConfig {

    public static final String OCR_QUEUE = "paperless.ocr.queue";

    // Declare the durable queue for OCR processing requests.
    @Bean
    public Queue ocrQueue() {
        return new Queue(OCR_QUEUE, true);
    }

    // Open a RabbitMQ connection when the application starts.
    @Bean
    public ApplicationRunner rabbitConnectionCheck(ConnectionFactory connectionFactory) {
        return args -> {
            connectionFactory.createConnection();
        };
    }
}