package com.manvi.productcatalogservice.dto;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DummyJsonProductListDto {
    private List<DummyJsonProductDto> products;
}
