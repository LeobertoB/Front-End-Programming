package com.eventhub.api.services;

import java.util.List;

import com.eventhub.api.dto.CategoryRequest;
import com.eventhub.api.dto.CategoryResponse;
import com.eventhub.api.mappers.ApiMapper;
import com.eventhub.domain.entities.Category;
import com.eventhub.repositories.CategoryRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CategoryService {

    private final CategoryRepository categoryRepository;

    public CategoryService(CategoryRepository categoryRepository) {
        this.categoryRepository = categoryRepository;
    }

    @Transactional(readOnly = true)
    public List<CategoryResponse> findAll() {
        return categoryRepository.findAll().stream().map(ApiMapper::toCategory).toList();
    }

    @Transactional
    public CategoryResponse create(CategoryRequest request) {
        categoryRepository.findByNameIgnoreCase(request.name()).ifPresent(existing -> {
            throw new IllegalArgumentException("Category name already exists");
        });
        Category category = new Category();
        apply(category, request);
        return ApiMapper.toCategory(categoryRepository.save(category));
    }

    @Transactional
    public CategoryResponse update(Long id, CategoryRequest request) {
        Category category = getEntity(id);
        apply(category, request);
        return ApiMapper.toCategory(category);
    }

    @Transactional
    public void delete(Long id) {
        categoryRepository.delete(getEntity(id));
    }

    public Category getEntity(Long id) {
        return categoryRepository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("Category not found"));
    }

    private void apply(Category category, CategoryRequest request) {
        category.setName(request.name().trim());
        category.setDescription(request.description().trim());
    }
}
