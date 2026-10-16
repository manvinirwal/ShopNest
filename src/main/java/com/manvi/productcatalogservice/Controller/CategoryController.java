package com.manvi.productcatalogservice.Controller;

import com.manvi.productcatalogservice.dto.CategoryDto;
import com.manvi.productcatalogservice.dto.ProductDto;
import com.manvi.productcatalogservice.model.Category;
import com.manvi.productcatalogservice.model.Product;
import com.manvi.productcatalogservice.service.ICategoryService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping("/categories")
public class CategoryController {

    private final ICategoryService categoryService;

    public CategoryController(ICategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/{category}")
    public ResponseEntity<Double> getAvgPriceForCategory(@PathVariable String category) {
        Double avg = categoryService.getAvgPriceForCategory(category);
        return new ResponseEntity<>(avg, HttpStatus.OK);
    }

    @GetMapping
    public ResponseEntity<List<CategoryDto>> getAllCategories() {
        List<Category> categories = categoryService.getAllCategories();
        List<CategoryDto> categoryDtos = new ArrayList<>();
        for(Category category: categories) {
            categoryDtos.add(from(category));
        }
        return new ResponseEntity<>(categoryDtos, HttpStatus.OK);
    }

    private CategoryDto from(Category category){
        CategoryDto categoryDto = new CategoryDto();
        categoryDto.setId(category.getId());
        categoryDto.setDescription(category.getDescription());
        categoryDto.setName(category.getName());

        return categoryDto;


    }

}