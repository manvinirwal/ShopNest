package com.manvi.productcatalogservice.mapper;


import com.manvi.productcatalogservice.dto.CategoryDto;
import com.manvi.productcatalogservice.dto.ProductDto;
import com.manvi.productcatalogservice.model.Category;
import com.manvi.productcatalogservice.model.Product;
import com.manvi.productcatalogservice.model.enums.State;
import org.springframework.stereotype.Component;

@Component
public class ProductMapper {

    public ProductDto toDto(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());

        if (product.getCategory() != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setName(product.getCategory().getName());
            categoryDto.setDescription(product.getCategory().getDescription());
            dto.setCategory(categoryDto);
        }
        return dto;
    }

    public Product applyPatch(Product existing, ProductDto patch) {
        if (patch == null) {
            return existing;
        }
        if (patch.getName() != null) {
            existing.setName(patch.getName());
        }
        if (patch.getPrice() != null) {
            existing.setPrice(patch.getPrice());
        }
        if (patch.getDescription() != null) {
            existing.setDescription(patch.getDescription());
        }
        if (patch.getImageUrl() != null) {
            existing.setImageUrl(patch.getImageUrl());
        }
        return existing;
    }

    public Product toEntity(ProductDto dto) {
        Product product = new Product();
        // product.setId(dto.getId());
        product.setName(dto.getName());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getImageUrl());

        if (dto.getCategory() != null) {
            Category category = new Category();
            category.setName(dto.getCategory().getName());
            category.setDescription(dto.getCategory().getDescription());
            category.setState(State.ACTIVE);
            product.setCategory(category);
        }
        return product;
    }
}