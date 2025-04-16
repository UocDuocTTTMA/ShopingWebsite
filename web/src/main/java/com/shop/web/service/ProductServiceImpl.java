package com.shop.web.service;

import com.shop.web.model.Product;
import com.shop.web.repository.ProductRepository;
import com.shop.web.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProductServiceImpl implements ProductService {

    @Autowired
    private ProductRepository productRepository;

    @Override
    public List<Product> findAllProducts() {
        return productRepository.findAll();
    }

    @Override
    public Optional<Product> findProductById(Long id) {
        return productRepository.findById(id);
    }

    @Override
    public Product saveProduct(Product product) {
        // Có thể thêm logic validate hoặc xử lý khác ở đây
        return productRepository.save(product);
    }

    @Override
    public void deleteProductById(Long id) {
        if (!productRepository.existsById(id)) {
             throw new RuntimeException("Error: Product not found with id " + id); // Nên dùng exception tùy chỉnh
        }
        productRepository.deleteById(id);
    }
    @Override
    public List<Product> findProductsByCategoryId(Long categoryId) {
        return productRepository.findByCategoryId(categoryId);
    }
}
