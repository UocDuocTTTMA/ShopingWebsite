package com.shop.web.service;

import com.shop.web.model.Category;
import com.shop.web.repository.CategoryRepository;
import com.shop.web.service.CategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
public class CategoryServiceImpl implements CategoryService {

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    public List<Category> findAllCategories() {
        return categoryRepository.findAll();
    }

    @Override
    public Optional<Category> findCategoryById(Long id) {
        return categoryRepository.findById(id);
    }

    @Override
    @Transactional // Đảm bảo transaction
    public Category saveCategory(Category category) {
         // Kiểm tra trùng tên khi tạo mới hoặc khi đổi tên (nếu ID khác)
         Optional<Category> existingCategory = categoryRepository.findByName(category.getName());
         if (existingCategory.isPresent() && !existingCategory.get().getId().equals(category.getId())) {
             throw new DataIntegrityViolationException("Tên danh mục '" + category.getName() + "' đã tồn tại.");
         }
        return categoryRepository.save(category);
    }

    @Override
    @Transactional
    public void deleteCategoryById(Long id) {
        if (isCategoryInUse(id)) {
             throw new DataIntegrityViolationException("Không thể xóa danh mục đang được sử dụng bởi sản phẩm.");
        }
        if (!categoryRepository.existsById(id)) {
             throw new RuntimeException("Error: Category not found with id " + id); // Nên dùng exception tùy chỉnh
        }
        categoryRepository.deleteById(id);
    }

    @Override
    public boolean isCategoryInUse(Long id) {
        // Sử dụng phương thức đã thêm trong repository
        return categoryRepository.existsByIdAndProductsIsNotEmpty(id);
        // Hoặc cách khác:
        // Optional<Category> category = categoryRepository.findById(id);
        // return category.isPresent() && !category.get().getProducts().isEmpty(); // Cần @Transactional nếu LAZY
    }

     @Override
    public boolean categoryExistsByName(String name) {
        return categoryRepository.existsByName(name);
    }
}
