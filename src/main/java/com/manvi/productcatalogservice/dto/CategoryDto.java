package com.manvi.productcatalogservice.dto;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryDto {

    private Long id;
    private String name;
    private String description;
    // CategoryDto does not contain List<ProductDto>, there is no circular reference in the DTO graph.
    // we don't need @JsonManagedReference or @JsonBackReference for this response.
}