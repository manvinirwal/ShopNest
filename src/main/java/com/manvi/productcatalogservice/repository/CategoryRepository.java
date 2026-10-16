package com.manvi.productcatalogservice.repository;

import com.manvi.productcatalogservice.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;

public interface CategoryRepository  extends JpaRepository<Category, Long> {

    Optional<Category> findByName(String name);
    // we can use derived query methods only for filtering and sorting puposes --for calculations we need to write jpql and mysql
    // prefer jpql -- loose coupling with mysql and also we know java entities well so easier to work with them
    @Query(value = "SELECT AVG(p.price) from Product p where p.category.name=:categoryName")
    Double findAvgPriceForCategory(String categoryName);

//    @Query(value = "SELECT avg(p.price) from product p JOIN catgegory c " +
//            "ON p.category_id = c.id " +
//            "where c.name=:categoryName",nativeQuery = true)
//    Double findAvgPriceForCategory(String categoryName);

}
