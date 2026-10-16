package com.manvi.productcatalogservice.service;


import com.manvi.productcatalogservice.dto.ProductDto;
import com.manvi.productcatalogservice.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;

public interface IProductService {


    public List<Product> getAllProducts();
    Product getProductById(Long id);

    Product createProduct(ProductDto productDto);

    boolean deleteProductById(Long id);


    public Page<Product> getProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) ;
}
