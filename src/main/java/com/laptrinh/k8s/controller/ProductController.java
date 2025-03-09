package com.laptrinh.k8s.controller;

import com.laptrinh.k8s.dtos.ProductDto;
import com.laptrinh.k8s.entities.Product;
import com.laptrinh.k8s.services.ProductService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@RestController
public class ProductController {

    private final ProductService productService;

    public ProductController(ProductService productService) {
        this.productService = productService;
    }

    @GetMapping("/products")
    public List<ProductDto> getAllProducts() {
        return productService.getAllProducts();
    }

    @GetMapping("/search-products")
    public List<ProductDto> searchProduct(
            @RequestParam(required = false, defaultValue = "") String keyword) throws IOException {
        return productService.getProductsWithSearchV2(keyword);
    }

}
