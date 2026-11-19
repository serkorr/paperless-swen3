package com.swen3.indexingworker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class IndexingWorkerApplication {

    // Start the indexing worker application.
    public static void main(String[] args) {
        SpringApplication.run(IndexingWorkerApplication.class, args);
    }
}
