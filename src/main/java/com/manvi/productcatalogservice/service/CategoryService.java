package com.manvi.productcatalogservice.service;

import com.manvi.productcatalogservice.model.Category;
import com.manvi.productcatalogservice.repository.CategoryRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CategoryService implements ICategoryService{

    private final CategoryRepository categoryRepo;

    public CategoryService(CategoryRepository categoryRepo) {
        this.categoryRepo = categoryRepo;
    }

    @Override
    public Double getAvgPriceForCategory(String categoryName) {
        return categoryRepo.findAvgPriceForCategory(categoryName);
    }

    @Override
    public List<Category> getAllCategories() {
        return categoryRepo.findAll();
    }
}