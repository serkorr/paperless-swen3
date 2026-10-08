package com.swen3.paperless.service;

import com.swen3.paperless.dto.CategoryRequest;
import com.swen3.paperless.dto.CategoryResponse;
import com.swen3.paperless.entity.Category;
import com.swen3.paperless.repository.CategoryRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryServiceTest {

    private CategoryRepository categoryRepository;
    private CategoryService categoryService;

    @BeforeEach
    void setUp() {
        categoryRepository = mock(CategoryRepository.class);
        categoryService = new CategoryService(categoryRepository);
    }

    // Verifies that a new category is saved successfully
    @Test
    void shouldCreateCategory() {
        CategoryRequest request = new CategoryRequest("University");
        Category category = new Category("University");

        when(categoryRepository.findByName("University"))
                .thenReturn(Optional.empty());
        when(categoryRepository.save(any(Category.class)))
                .thenReturn(category);

        CategoryResponse response = categoryService.createCategory(request);

        assertEquals("University", response.name());
        verify(categoryRepository).findByName("University");
        verify(categoryRepository).save(any(Category.class));
    }

    // Verifies that duplicate category names are rejected
    @Test
    void shouldRejectDuplicateCategoryName() {
        CategoryRequest request = new CategoryRequest("University");

        when(categoryRepository.findByName("University"))
                .thenReturn(Optional.of(new Category("University")));

        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> categoryService.createCategory(request)
        );

        assertEquals("Category already exists: University",
                exception.getMessage());
        verify(categoryRepository, never()).save(any(Category.class));
    }

    // Verifies that all categories are returned
    @Test
    void shouldGetAllCategories() {
        when(categoryRepository.findAll()).thenReturn(List.of(
                new Category("University"),
                new Category("Invoices")
        ));

        List<CategoryResponse> responses =
                categoryService.getAllCategories();

        assertEquals(2, responses.size());
        assertEquals("University", responses.get(0).name());
        assertEquals("Invoices", responses.get(1).name());
        verify(categoryRepository).findAll();
    }

    // Verifies that an empty list is returned when no categories exist
    @Test
    void shouldReturnEmptyCategoryList() {
        when(categoryRepository.findAll()).thenReturn(List.of());

        List<CategoryResponse> responses =
                categoryService.getAllCategories();

        assertTrue(responses.isEmpty());
        verify(categoryRepository).findAll();
    }

    // Verifies that a category can be retrieved by ID
    @Test
    void shouldGetCategoryById() {
        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(new Category("University")));

        CategoryResponse response = categoryService.getCategoryById(1L);

        assertEquals("University", response.name());
        verify(categoryRepository).findById(1L);
    }

    // Verifies that requesting a missing category throws an exception
    @Test
    void shouldThrowExceptionWhenCategoryNotFound() {
        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        NoSuchElementException exception = assertThrows(
                NoSuchElementException.class,
                () -> categoryService.getCategoryById(999L)
        );

        assertEquals("Category not found with id: 999",
                exception.getMessage());
    }

    // Verifies that an existing category can be deleted
    @Test
    void shouldDeleteCategory() {
        Category category = new Category("University");

        when(categoryRepository.findById(1L))
                .thenReturn(Optional.of(category));

        categoryService.deleteCategory(1L);

        verify(categoryRepository).findById(1L);
        verify(categoryRepository).delete(category);
    }

    // Verifies that a missing category cannot be deleted
    @Test
    void shouldRejectDeletingMissingCategory() {
        when(categoryRepository.findById(999L))
                .thenReturn(Optional.empty());

        assertThrows(
                NoSuchElementException.class,
                () -> categoryService.deleteCategory(999L)
        );

        verify(categoryRepository, never()).delete(any(Category.class));
    }
}