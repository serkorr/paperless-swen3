package com.swen3.indexingworker.config;

import org.springframework.amqp.core.Queue;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RabbitMqConfig {

    // Declare a durable queue for OCR results.
    @Bean
    public Queue indexingQueue() {
        return new Queue("paperless.indexing.queue", true);
    }
}