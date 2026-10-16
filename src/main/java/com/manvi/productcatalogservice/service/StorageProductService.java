package com.manvi.productcatalogservice.service;

import com.manvi.productcatalogservice.dto.CategoryDto;
import com.manvi.productcatalogservice.dto.ProductDto;
import com.manvi.productcatalogservice.model.Category;
import com.manvi.productcatalogservice.model.Product;
import com.manvi.productcatalogservice.model.enums.State;
import com.manvi.productcatalogservice.repository.CategoryRepository;
import com.manvi.productcatalogservice.repository.ProductRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Optional;


@Service
@Primary
@Slf4j
public class StorageProductService implements  IProductService{

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepo;

    public StorageProductService(ProductRepository productRepository, CategoryRepository categoryRepo) {
        this.productRepository = productRepository;
        this.categoryRepo = categoryRepo;
    }

    @Override
    public List<Product> getAllProducts() {
        return List.of();
    }

    @Override
    public Product getProductById(Long id) {
        log.info("Reading product from DB: "+id);
        return productRepository.findByIdAndState(id, State.ACTIVE).orElse(null);
    }

    @Override
    public Product createProduct(ProductDto productDto) {
        Product product = from(productDto);
        // if category row already exists then do'not create duplicate category record-- if exists use that ow  use newly created ctagory obj in from helper method
        product.setCategory(findOrCreateCategory(product.getCategory()));
        product.setCreatedAt(new Date());
        product.setLastUpdatedAt(new Date());
        product.setState(State.ACTIVE);
        return productRepository.save(product);
    }

    @Override
    public boolean deleteProductById(Long id) {
        Product product = productRepository.findById(id).orElse(null);
        if(product==null) return false;
        // we  support Soft delete
        product.setState(State.INACTIVE);
        product.setLastUpdatedAt(new Date());
        productRepository.save(product);
        return true;
    }

    @Override
    public Product updateProduct(Long id, ProductDto productDto) {
        Product existingProduct = productRepository.findById(id).orElse(null);
        if (existingProduct == null) {
            return null;
        }

        Product updatedProduct = from(productDto);

        existingProduct.setName(updatedProduct.getName());
        existingProduct.setDescription(updatedProduct.getDescription());
        existingProduct.setPrice(updatedProduct.getPrice());
        existingProduct.setImageUrl(updatedProduct.getImageUrl());

        if (updatedProduct.getCategory() != null) {
            existingProduct.setCategory(
                    findOrCreateCategory(updatedProduct.getCategory())
            );
        }

        existingProduct.setLastUpdatedAt(new Date());


        return productRepository.save(existingProduct);
    }

    private Category findOrCreateCategory(Category category) {
        if (category == null) {
            return null;
        }

        Optional<Category> existing = categoryRepo.findByName(category.getName());
        if (existing.isPresent()) {
            return existing.get();
        }

        Date now = new Date();
        category.setId(null);
        category.setCreatedAt(now);
        category.setLastUpdatedAt(now);
        return category;
    }

    private Product from(ProductDto dto) {
        Product product = new Product();
        product.setId(dto.getId());
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



    @Override
    public Page<Product> getProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return productRepository.findByPriceBetweenAndState(minPrice,
                maxPrice,
                State.ACTIVE,
                pageable);
    }
}
