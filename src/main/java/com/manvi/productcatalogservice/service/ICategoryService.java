package com.manvi.productcatalogservice.service;

import com.manvi.productcatalogservice.dto.CategoryDto;
import com.manvi.productcatalogservice.model.Category;
import org.springframework.http.ResponseEntity;

import java.util.List;

public interface ICategoryService {

    public Double getAvgPriceForCategory(String categoryName);

    public List<Category> getAllCategories();
}
