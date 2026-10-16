package com.manvi.productcatalogservice.service;

import com.manvi.productcatalogservice.dto.DummyJsonProductDto;
import com.manvi.productcatalogservice.dto.DummyJsonProductListDto;
import com.manvi.productcatalogservice.dto.ProductDto;
import com.manvi.productcatalogservice.model.Category;
import com.manvi.productcatalogservice.model.Product;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.restclient.RestTemplateBuilder;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;


@Service
@Qualifier
public class DummyProductService implements IProductService{


    private RestTemplate restTemplate;

    public DummyProductService(RestTemplateBuilder builder) {
        this.restTemplate = builder.build();
    }



    @Override
    public Product getProductById(Long id) {
        try {
            ResponseEntity<DummyJsonProductDto> response = restTemplate.getForEntity("https://dummyjson.com/products/{id}",
                    DummyJsonProductDto.class,
                    id);
            if(response.getStatusCode().is2xxSuccessful() && response.hasBody()) {
                return from(response.getBody());
            }
            return null;
        } catch (HttpClientErrorException.NotFound e) {
            return null;
        }
    }

    @Override
    public Product createProduct(ProductDto productDto) {
        return null;
    }

    @Override
    public boolean deleteProductById(Long id) {
        return false;
    }

    @Override
    public Product updateProduct(Long id, ProductDto productDto) {
        return null;
    }

    @Override
    public Page<Product> getProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable) {
        return null;
    }

    @Override
    public List<Product> getAllProducts() {
        //separate DummyJsonProductListDto because the external API returns a JSON object containing a list of products, not a JSON array directly.
        ResponseEntity<DummyJsonProductListDto> response = restTemplate.getForEntity(
                "https://dummyjson.com/products?limit=0", DummyJsonProductListDto.class);

        List<Product> products = new ArrayList<>();
        for (DummyJsonProductDto dto : response.getBody().getProducts()) {
            products.add(from(dto));
        }
        return products;
    }

    private Product from(DummyJsonProductDto dto) {
        Product product = new Product();
        product.setId(dto.getId());
        product.setName(dto.getTitle());
        product.setDescription(dto.getDescription());
        product.setPrice(dto.getPrice());
        product.setImageUrl(dto.getThumbnail());

        Category category = new Category();
        category.setName(dto.getCategory());
        product.setCategory(category);
        return product;
    }
}
