package com.shop.web.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "categories")
@Getter
@Setter
@NoArgsConstructor
public class Category {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank(message = "Tên danh mục không được để trống")
    @Column(length = 100, nullable = false, unique = true)
    private String name;

    // CascadeType.PERSIST: Khi lưu Category, nếu có Product mới trong Set này cũng sẽ được lưu.
    // CascadeType.MERGE: Khi cập nhật Category, nếu có Product thay đổi trong Set cũng sẽ được cập nhật.
    // orphanRemoval = true: Khi xóa Product khỏi Set này (category.getProducts().remove(product)), Product đó sẽ bị xóa khỏi DB.
    // mappedBy = "category": Chỉ ra rằng mối quan hệ được quản lý bởi thuộc tính 'category' trong lớp Product.
    @OneToMany(mappedBy = "category", cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    private Set<Product> products = new HashSet<>();

    public Category(String name) {
        this.name = name;
    }

    // Helper methods (tùy chọn)
    public void addProduct(Product product) {
        this.products.add(product);
        product.setCategory(this);
    }

    public void removeProduct(Product product) {
        this.products.remove(product);
        product.setCategory(null);
    }

     @Override
    public String toString() {
        return name; // Giúp hiển thị tên trong dropdown dễ dàng hơn
    }

    // Cần equals và hashCode nếu dùng Set và muốn so sánh các đối tượng Category
     @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Category )) return false;
        return id != null && id.equals(((Category) o).getId());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
