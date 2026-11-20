package com.manvi.productcatalogservice.mapper;

import com.manvi.productcatalogservice.dto.CategoryDto;
import com.manvi.productcatalogservice.dto.ProductDto;
import com.manvi.productcatalogservice.model.Category;
import com.manvi.productcatalogservice.model.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;

class ProductMapperTest {



    private ProductMapper mapper;



    @BeforeEach

    void setUp() {

        mapper = new ProductMapper();

    }



    @Test

    @DisplayName("toDto copies the fields and the category")

    void toDto_product_copiesFieldsAndCategory() {

        Category category = new Category();

        category.setName("beauty");

        Product product = new Product();

        product.setId(1L);

        product.setName("Mascara");

        product.setPrice(new BigDecimal("9.99"));

        product.setCategory(category);



        ProductDto dto = mapper.toDto(product);



        assertThat(dto.getId()).isEqualTo(1L);

        assertThat(dto.getName()).isEqualTo("Mascara");

        assertThat(dto.getPrice()).isEqualTo(new BigDecimal("9.99"));

        assertThat(dto.getCategory().getName()).isEqualTo("beauty");

    }



    @Test

    void toDto_noCategory_keepsCategoryNull() {

        Product product = new Product();

        product.setName("Mascara");



        ProductDto dto = mapper.toDto(product);



        assertThat(dto.getCategory()).isNull();

    }



    @Test

    void toDto_nullProduct_returnsNull() {

        assertThat(mapper.toDto(null)).isNull();

    }



    @Test

    void toEntity_dto_copiesFieldsAndCategory() {

        CategoryDto categoryDto = new CategoryDto();

        categoryDto.setName("home");

        ProductDto dto = new ProductDto();

        dto.setName("Desk Lamp");

        dto.setPrice(new BigDecimal("499.50"));

        dto.setCategory(categoryDto);



        Product product = mapper.toEntity(dto);



        assertThat(product.getName()).isEqualTo("Desk Lamp");

        assertThat(product.getPrice()).isEqualTo(new BigDecimal("499.50"));

        assertThat(product.getCategory().getName()).isEqualTo("home");

    }

}
