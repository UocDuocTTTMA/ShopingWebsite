package com.shop.web.service;

import com.shop.web.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductService {
    List<Product> findAllProducts();
    Optional<Product> findProductById(Long id);
    Product saveProduct(Product product);
    void deleteProductById(Long id);
    List<Product> findProductsByCategoryId(Long categoryId);
}