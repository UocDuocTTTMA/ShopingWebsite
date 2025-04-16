package com.shop.web.controllers;
import com.shop.web.model.Category;
import com.shop.web.service.CategoryService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;
import java.util.Optional;

@Controller
@RequestMapping("/admin/categories") // Đặt prefix /admin/categories cho tất cả các mapping trong controller này
@PreAuthorize("hasRole('ADMIN')") // Yêu cầu quyền ADMIN cho tất cả các phương thức
public class CategoryController {

    @Autowired
    private CategoryService categoryService;

    // Hiển thị danh sách categories
    @GetMapping
    public String listCategories(Model model) {
        List<Category> categories = categoryService.findAllCategories();
        model.addAttribute("categories", categories);
        return "admin/category-list"; // View: templates/admin/category-list.html
    }

    // Hiển thị form thêm category
    @GetMapping("/new")
    public String showCreateCategoryForm(Model model) {
        model.addAttribute("category", new Category());
        model.addAttribute("pageTitle", "Thêm Danh Mục Mới");
        return "admin/category-form"; // View: templates/admin/category-form.html
    }

    // Xử lý lưu category (Thêm mới hoặc Cập nhật)
    @PostMapping("/save")
    public String saveCategory(@Valid @ModelAttribute("category") Category category,
                               BindingResult result,
                               RedirectAttributes redirectAttributes,
                               Model model) {
        // Kiểm tra lỗi validation cơ bản (NotBlank)
        if (result.hasErrors()) {
             model.addAttribute("pageTitle", (category.getId() == null) ? "Thêm Danh Mục Mới" : "Chỉnh Sửa Danh Mục");
            return "admin/category-form";
        }

        try {
            categoryService.saveCategory(category);
            redirectAttributes.addFlashAttribute("successMessage", "Lưu danh mục thành công!");
            return "redirect:/admin/categories"; // Redirect về trang danh sách
        } catch (DataIntegrityViolationException e) {
            // Bắt lỗi nếu tên danh mục bị trùng (đã xử lý trong service)
            result.rejectValue("name", "error.category", e.getMessage());
            model.addAttribute("pageTitle", (category.getId() == null) ? "Thêm Danh Mục Mới" : "Chỉnh Sửa Danh Mục");
            return "admin/category-form";
        } catch (Exception e) {
            // Các lỗi khác
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi lưu danh mục: " + e.getMessage());
             model.addAttribute("pageTitle", (category.getId() == null) ? "Thêm Danh Mục Mới" : "Chỉnh Sửa Danh Mục");
            return "admin/category-form";
        }
    }

    // Hiển thị form sửa category
    @GetMapping("/edit/{id}")
    public String showEditCategoryForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Category> categoryOpt = categoryService.findCategoryById(id);
        if (categoryOpt.isPresent()) {
            model.addAttribute("category", categoryOpt.get());
            model.addAttribute("pageTitle", "Chỉnh Sửa Danh Mục");
            return "admin/category-form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy danh mục với ID: " + id);
            return "redirect:/admin/categories";
        }
    }

    // Xử lý xóa category
    @GetMapping("/delete/{id}")
    public String deleteCategory(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            categoryService.deleteCategoryById(id);
            redirectAttributes.addFlashAttribute("successMessage", "Xóa danh mục thành công!");
        } catch (DataIntegrityViolationException e) {
             redirectAttributes.addFlashAttribute("errorMessage", e.getMessage()); // Lấy thông báo lỗi từ service
        } catch (Exception e) {
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa danh mục: " + e.getMessage());
        }
        return "redirect:/admin/categories";
    }
}