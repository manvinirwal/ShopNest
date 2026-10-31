package com.manvi.productcatalogservice.repository;

import com.manvi.productcatalogservice.model.Product;
import com.manvi.productcatalogservice.model.enums.State;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public  interface ProductRepository extends JpaRepository<Product, Long> {


    Optional<Product> findByIdAndState(Long id, State state);



  // solves N+1 query problem in hibernate-- tells to fetch eagerly instead of fetching category in another N queries
    // this is better than  FetchType.EAGER at enitiy level
  // Using FetchType.EAGER makes the category relationship permanently eager across your entire application, whereas @EntityGraph is a temporary override for specific queries.
    // if multilple fetching-@EntityGraph(attributePaths = {"category", "supplier", "manufacturer"})
    @EntityGraph(attributePaths = {"category"})
    Page<Product> findByPriceBetweenAndState(BigDecimal min, BigDecimal max, State state, Pageable pageable);



    // @EntityGraph(attributePaths = {"category"})
    //    List<Product> findAll();

    // other option is using jpql to tell fetch eagerly
    @Query("SELECT p from Product p LEFT JOIN FETCH p.category c")
    List<Product> findAllProducts();


}
