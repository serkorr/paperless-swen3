package com.swen3.paperless.repository;

import com.swen3.paperless.entity.Document;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {

}