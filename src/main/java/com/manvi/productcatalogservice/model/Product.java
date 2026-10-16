package com.manvi.productcatalogservice.model;


import com.fasterxml.jackson.annotation.JsonIdentityInfo;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;


@Getter
@Setter
@Entity
@Table(indexes = @Index(name = "idx_product_name",columnList = "name"))
public class Product extends  BaseEntity{

    private String name;


    @Column(precision =  19, scale =3)
    private BigDecimal price; // BigDecimal --> very very precise

    private String description;

    private String imageUrl;


    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "category_id")
    private Category category;
}
