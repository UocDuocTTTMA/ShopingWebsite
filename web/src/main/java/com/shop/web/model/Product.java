package com.shop.web.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull; // Thêm import này
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import java.math.BigDecimal;

@Entity
@Table(name = "products")
@Getter
@Setter
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 200)
    private String name;

    @Lob
    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(length = 255)
    private String imageUrl;

    // --- THÊM MỐI QUAN HỆ MANY-TO-ONE ---
    @NotNull(message = "Danh mục không được để trống") // Đảm bảo sản phẩm phải có danh mục
    @ManyToOne(fetch = FetchType.LAZY) // LAZY để không load Category ngay khi load Product (trừ khi cần)
    @JoinColumn(name = "category_id", nullable = false) // Tên cột khóa ngoại trong bảng products, không cho phép null
    private Category category;
    // --- KẾT THÚC PHẦN THÊM ---

    // Constructor cũ (có thể giữ lại hoặc cập nhật)
    public Product(String name, String description, BigDecimal price, String imageUrl) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
    }

     // Constructor mới với Category
     public Product(String name, String description, BigDecimal price, String imageUrl, Category category) {
        this.name = name;
        this.description = description;
        this.price = price;
        this.imageUrl = imageUrl;
        this.category = category;
    }

     // Bỏ phương thức toString() nếu có vì có thể gây lỗi LazyInitializationException
     // Hoặc chỉ toString các thuộc tính cơ bản, không bao gồm category

}