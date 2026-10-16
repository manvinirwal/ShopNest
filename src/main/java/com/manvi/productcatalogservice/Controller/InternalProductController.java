package com.manvi.productcatalogservice.Controller;

import com.manvi.productcatalogservice.service.ProductImportService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/internal/products")
public class InternalProductController {

    // contains APIs which will be called internally not by customers

    private final ProductImportService productImportService;

    public InternalProductController(
            ProductImportService productImportService) {
        this.productImportService = productImportService;
    }

    // this is endpoint to load data from  dummJsomAPI to our database--this will be called onlly once to import data
    @PostMapping("/import")
    public ResponseEntity<String> importProducts() {
        int count = productImportService.importProducts();
        return ResponseEntity.ok("Imported " + count + " products");
    }
}