package com.shop.web.service;

import com.shop.web.model.Category;
import java.util.List;
import java.util.Optional;

public interface CategoryService {
    List<Category> findAllCategories();
    Optional<Category> findCategoryById(Long id);
    Category saveCategory(Category category);
    void deleteCategoryById(Long id);
    boolean isCategoryInUse(Long id); // Kiểm tra xem category có đang được sản phẩm nào dùng không
    boolean categoryExistsByName(String name);
}