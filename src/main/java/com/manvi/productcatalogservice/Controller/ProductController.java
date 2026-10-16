package com.manvi.productcatalogservice.Controller;


import com.manvi.productcatalogservice.dto.CategoryDto;
import com.manvi.productcatalogservice.dto.ProductDto;
import com.manvi.productcatalogservice.model.Product;
import com.manvi.productcatalogservice.service.IProductService;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PagedModel;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

@RestController
@RequestMapping("/products")
public class ProductController {

    private final IProductService productService;
    private final IProductService supplierService;
    // we are using dummyjson api to get product data for database--so dummyProductService acts as supplier of data



    public ProductController(IProductService productService,
                             @Qualifier("dummyProductService") IProductService supplierService) {
        this.productService = productService;
        this.supplierService = supplierService;
    }



    @GetMapping
    ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDto> productDtos = new ArrayList<>();
        for(Product product: products) {
            productDtos.add(from(product));
        }
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    ResponseEntity<ProductDto> getProductById(@PathVariable("id") Long id) {
        if (id < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        //Product product = productService.getProductById(id);
         Product product = supplierService.getProductById(id);

        if (product == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }
        return new ResponseEntity<>(from(product), HttpStatus.OK);
    }


    private ProductDto from(Product product) {
        ProductDto dto = new ProductDto();
        dto.setId(product.getId());
        dto.setName(product.getName());
        dto.setDescription(product.getDescription());
        dto.setPrice(product.getPrice());
        dto.setImageUrl(product.getImageUrl());

        if (product.getCategory() != null) {
            CategoryDto categoryDto = new CategoryDto();
            categoryDto.setId(product.getCategory().getId());
            categoryDto.setName(product.getCategory().getName());
            categoryDto.setDescription(product.getCategory().getDescription());
            dto.setCategory(categoryDto);
        }
        return dto;
    }

    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        Product product = productService.createProduct(productDto);
        return new ResponseEntity<>(from(product), HttpStatus.CREATED);
    }

    @DeleteMapping("{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable Long id) {
        boolean isDeleted = productService.deleteProductById(id);
        if (isDeleted) return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        return new ResponseEntity<>(HttpStatus.NOT_FOUND);
    }



    /*
     * Page<T>:
     * - Provided by Spring Data.
     * - Represents a page of data along with pagination metadata
     *   such as current page number, total pages, and total elements.
     * - Typically used in repository and service layers.
     *
     * PagedModel<T>:
     * - Provided by Spring HATEOAS.
     * - Represents paginated data in an API-friendly format.
     * - Contains the page content and pagination metadata.
     * - Supports navigation links such as next and previous when
     *   configured using PagedResourcesAssembler or added manually.
     * - Typically used in the controller layer to format API responses.
     *
     * Difference:
     * - Page<T> represents paginated query results.
     * - PagedModel<T> represents paginated API resources.
     *
     * Note:
     * - Returning Page<T> or PagedModel<T> directly from a controller
     *   does not require ResponseEntity.
     * - Use ResponseEntity when explicit control over HTTP status
     *   codes or headers is needed.
     */
    /*
     * Page<T>:
     * - Provides pagination metadata through methods such as:
     *   getNumber()        -> Current page number (0-based)
     *   getSize()          -> Requested page size
     *   getTotalElements() -> Total number of matching elements
     *   getTotalPages()    -> Total number of pages
     *   hasNext()          -> Whether a next page exists
     *   hasPrevious()      -> Whether a previous page exists
     *
     * PagedModel<T>:
     * - Exposes pagination metadata through its page metadata object:
     *   getMetadata().getNumber()        -> Current page number (1-based)
     *   getMetadata().getSize()          -> Page size
     *   getMetadata().getTotalElements() -> Total number of elements
     *   getMetadata().getTotalPages()    -> Total number of pages
     *
     * Difference:
     * - Page is a Spring Data abstraction for paginated query results.
     * - PagedModel is a Spring HATEOAS representation for paginated API responses.
     * - PagedModel can also include navigation links when configured.
     */


    //GET http://localhost:8080/products/search?minPrice=100&maxPrice=1000&page=0&size=10&sort=price,asc
    @GetMapping("/search")
    public PagedModel<ProductDto> getProductsInPriceRange(
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            Pageable pageable) {

        Page<ProductDto> products = productService
                .getProductsInPriceRange(minPrice,maxPrice,pageable)
                .map(x -> from(x));

        return new PagedModel<>(products);

    }








}
