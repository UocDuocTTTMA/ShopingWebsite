package com.shop.web.repository;

import com.shop.web.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    Optional<Category> findByName(String name);
    boolean existsByName(String name);

    // Thêm phương thức kiểm tra xem Category có sản phẩm nào không (để kiểm tra trước khi xóa)
    // Cách 1: Dùng count query
    // @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId")
    // long countProductsByCategoryId(@Param("categoryId") Long categoryId);

    // Cách 2: Dùng exists query (thường hiệu quả hơn nếu chỉ cần biết có hay không)
    boolean existsByIdAndProductsIsNotEmpty(Long id);
}