package com.manvi.productcatalogservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;

@Getter
@Setter
public class DummyJsonProductDto {

    private Long id;
    private String title;
    private String description;
    private BigDecimal price;
    private String category;
    private String thumbnail;

}