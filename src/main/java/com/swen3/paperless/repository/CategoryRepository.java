package com.swen3.paperless.repository;

import com.swen3.paperless.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Find a category by its unique name
    Optional<Category> findByName(String name);
}