package com.manvi.productcatalogservice.service;

import com.manvi.productcatalogservice.dto.CategoryDto;
import com.manvi.productcatalogservice.dto.ProductDto;
import com.manvi.productcatalogservice.model.Product;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Service;

@Service
public class ProductImportService {

    private final IProductService productService;
    private final IProductService supplierService;

    public ProductImportService(
            IProductService productService,
            @Qualifier("dummyProductService") IProductService supplierService) {
        this.productService = productService;
        this.supplierService = supplierService;
    }

    public int importProducts() {
        int count = 0;
        for (Product supplierProduct : supplierService.getAllProducts()) {
            ProductDto dto = toImportDto(supplierProduct);
            productService.createProduct(dto);
            count++;
        }

        return count;
    }

    private ProductDto toImportDto(Product product) {
        ProductDto dto = new ProductDto();
        //   dto.setId(product.getId());-- let our db set id do'nt use imported id
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());

        if (product.getCategory() != null) {
            CategoryDto categoryDto = new CategoryDto();
            // categoryDto.setId(product.getCategory().getId());
            categoryDto.setName(product.getCategory().getName());
            categoryDto.setDescription(product.getCategory().getDescription());
            dto.setCategory(categoryDto);
        }
        return dto;
    }
}