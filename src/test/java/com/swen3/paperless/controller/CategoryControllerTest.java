package com.swen3.paperless.controller;

import com.swen3.paperless.dto.CategoryRequest;
import com.swen3.paperless.dto.CategoryResponse;
import com.swen3.paperless.service.CategoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CategoryControllerTest {

    private CategoryService categoryService;
    private CategoryController categoryController;

    @BeforeEach
    void setUp() {
        categoryService = mock(CategoryService.class);
        categoryController = new CategoryController(categoryService);
    }

    // Verifies that creating a category returns HTTP 201 and its location
    @Test
    void shouldCreateCategory() {
        CategoryRequest request = new CategoryRequest("University");
        CategoryResponse expected = new CategoryResponse(1L, "University");

        when(categoryService.createCategory(request))
                .thenReturn(expected);

        ResponseEntity<CategoryResponse> response =
                categoryController.createCategory(request);

        assertEquals(HttpStatus.CREATED, response.getStatusCode());
        assertEquals(expected, response.getBody());
        assertEquals(
                "/api/categories/1",
                response.getHeaders().getLocation().toString()
        );

        verify(categoryService).createCategory(request);
    }

    // Verifies that all categories are returned from the service
    @Test
    void shouldGetAllCategories() {
        List<CategoryResponse> expected = List.of(
                new CategoryResponse(1L, "University"),
                new CategoryResponse(2L, "Invoices")
        );

        when(categoryService.getAllCategories())
                .thenReturn(expected);

        List<CategoryResponse> response =
                categoryController.getAllCategories();

        assertEquals(2, response.size());
        assertEquals(expected, response);

        verify(categoryService).getAllCategories();
    }

    // Verifies that an empty category list is returned when no categories exist
    @Test
    void shouldReturnEmptyCategoryList() {
        when(categoryService.getAllCategories())
                .thenReturn(List.of());

        List<CategoryResponse> response =
                categoryController.getAllCategories();

        assertTrue(response.isEmpty());
        verify(categoryService).getAllCategories();
    }

    // Verifies that a category can be retrieved by its ID
    @Test
    void shouldGetCategoryById() {
        CategoryResponse expected = new CategoryResponse(1L, "University");

        when(categoryService.getCategoryById(1L))
                .thenReturn(expected);

        CategoryResponse response =
                categoryController.getCategoryById(1L);

        assertEquals(expected, response);
        verify(categoryService).getCategoryById(1L);
    }

    // Verifies that deleting a category calls the service
    @Test
    void shouldDeleteCategory() {
        categoryController.deleteCategory(1L);

        verify(categoryService).deleteCategory(1L);
    }

    // Verifies that a missing category produces an HTTP 404 response
    @Test
    void shouldReturnNotFoundForMissingCategory() {
        NoSuchElementException exception =
                new NoSuchElementException("Category not found with id: 999");

        ResponseEntity<String> response =
                categoryController.handleNotFound(exception);

        assertEquals(HttpStatus.NOT_FOUND, response.getStatusCode());
        assertEquals(
                "Category not found with id: 999",
                response.getBody()
        );
    }

    // Verifies that duplicate categories produce an HTTP 409 response
    @Test
    void shouldReturnConflictForDuplicateCategory() {
        IllegalArgumentException exception =
                new IllegalArgumentException("Category already exists: University");

        ResponseEntity<String> response =
                categoryController.handleDuplicateCategory(exception);

        assertEquals(HttpStatus.CONFLICT, response.getStatusCode());
        assertEquals(
                "Category already exists: University",
                response.getBody()
        );
    }
}