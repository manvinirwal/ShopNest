package com.manvi.productcatalogservice.Controller;


import com.manvi.productcatalogservice.dto.CategoryDto;
import com.manvi.productcatalogservice.dto.ProductDto;
import com.manvi.productcatalogservice.dto.ProductPitchDto;
import com.manvi.productcatalogservice.mapper.ProductMapper;
import com.manvi.productcatalogservice.model.Product;
import com.manvi.productcatalogservice.service.IProductService;
import com.manvi.productcatalogservice.service.PitchService;
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
    private final ProductMapper productMapper;
    private  final PitchService pitchService;



    public ProductController(IProductService productService,
                             @Qualifier("dummyProductService") IProductService supplierService, ProductMapper productMapper, PitchService pitchService) {
        this.productService = productService;
        this.supplierService = supplierService;
        this.productMapper = productMapper;
        this.pitchService = pitchService;
    }



    @GetMapping
    ResponseEntity<List<ProductDto>> getAllProducts() {
        List<Product> products = productService.getAllProducts();
        List<ProductDto> productDtos = new ArrayList<>();
        for(Product product: products) {
            productDtos.add(productMapper.toDto(product));
        }
        return new ResponseEntity<>(productDtos, HttpStatus.OK);
    }

    @GetMapping("/{id}")
    ResponseEntity<ProductDto> getProductById(@PathVariable("id") Long id) {
        if (id < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

         Product product = productService.getProductById(id);
         //Product product = supplierService.getProductById(id);

        if (product == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);

        }
        return new ResponseEntity<>(productMapper.toDto(product), HttpStatus.OK);
    }

    @GetMapping("/{id}/pitch")
    public ResponseEntity<ProductPitchDto> generatePitch(@PathVariable Long id) {
        if (id < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Product product = productService.getProductById(id);
        if (product == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }

        ProductPitchDto productPitchDto = new ProductPitchDto();
        productPitchDto.setId(product.getId());
        productPitchDto.setPitch(pitchService.generatePitch(product));
        return new ResponseEntity<>(productPitchDto, HttpStatus.OK);

    }






    @PostMapping
    public ResponseEntity<ProductDto> createProduct(@RequestBody ProductDto productDto) {
        Product product = productService.createProduct(productDto);
        return new ResponseEntity<>(productMapper.toDto(product), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProductDto> updateProduct(
            @PathVariable Long id,
            @RequestBody ProductDto productDto) {

        if (id < 0) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }

        Product updatedProduct = productService.updateProduct(id, productDto);
        if(updatedProduct == null){
            return ResponseEntity.notFound().build(); // No body -> requires .build()
        }


        return ResponseEntity.ok(productMapper.toDto(updatedProduct));  // Has body -> do NOT use .build()
    }



    @PatchMapping("/{id}")
    public ResponseEntity<ProductDto> patchProduct(@PathVariable("id") Long id,
                                                    @RequestBody ProductDto productDto) {
        if (id <= 0) {
            return ResponseEntity.badRequest().build();
        }
        Product product = productService.patchProduct(id, productDto);
        if (product == null) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
        return new ResponseEntity<>(productMapper.toDto(product), HttpStatus.OK);
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
                .map(x -> productMapper.toDto(x));

        return new PagedModel<>(products);

    }








}
