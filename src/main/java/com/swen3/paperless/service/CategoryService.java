package com.swen3.paperless.service;

import com.swen3.paperless.dto.CategoryRequest;
import com.swen3.paperless.dto.CategoryResponse;
import com.swen3.paperless.entity.Category;
import com.swen3.paperless.repository.CategoryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;

@Service
@Transactional(readOnly = true)
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    // Creates a category only if its name is not already in use
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        if (categoryRepository.findByName(request.name()).isPresent()) {
            throw new IllegalArgumentException(
                    "Category already exists: " + request.name()
            );
        }

        Category category = new Category(request.name());
        Category savedCategory = categoryRepository.save(category);

        return toResponse(savedCategory);
    }

    // Returns all existing categories
    public List<CategoryResponse> getAllCategories() {
        return categoryRepository.findAll()
                .stream()
                .map(this::toResponse)
                .toList();
    }

    // Returns a category by its ID
    public CategoryResponse getCategoryById(Long id) {
        Category category = findCategoryOrThrow(id);
        return toResponse(category);
    }

    // Deletes a category if it exists
    @Transactional
    public void deleteCategory(Long id) {
        Category category = findCategoryOrThrow(id);
        categoryRepository.delete(category);
    }

    private Category findCategoryOrThrow(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException(
                        "Category not found with id: " + id
                ));
    }

    private CategoryResponse toResponse(Category category) {
        return new CategoryResponse(
                category.getId(),
                category.getName()
        );
    }
}