package com.shop.web.controllers;

import com.shop.web.dto.ProductDto; // Dùng cho API
import com.shop.web.model.Product;
import com.shop.web.model.Category;
import com.shop.web.service.CategoryService;
import com.shop.web.service.ProductService;
import jakarta.validation.Valid;
import org.springframework.beans.BeanUtils; // Dùng để copy properties
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Cách khác để phân quyền thay vì SecurityConfig
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.util.StringUtils;
import org.springframework.validation.BindingResult;
import java.util.UUID;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Controller
public class ProductController {

    @Autowired
    private ProductService productService;
    @Autowired
    private CategoryService categoryService;

    private static final String UPLOAD_DIR = "src/main/resources/static/uploads/";
    @GetMapping("/products")
    public String listProducts(@RequestParam(value = "categoryId", required = false) Long categoryId, Model model) {
        List<Product> products = productService.findAllProducts();
        List<Category> categories = categoryService.findAllCategories();
        if (categoryId != null) {
             products = productService.findAllProducts().stream()
                     .filter(p -> p.getCategory() != null && p.getCategory().getId().equals(categoryId))
                     .collect(Collectors.toList());
            model.addAttribute("selectedCategoryId", categoryId);
        } else {
            products = productService.findAllProducts();
        }

        model.addAttribute("products", products);
        model.addAttribute("categories", categories); 
        return "products/list";
    }
    @GetMapping("/admin/products/new")
    public String showAddProductForm(Model model) {
        model.addAttribute("product", new Product()); 
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("pageTitle", "Thêm sản phẩm mới");
        return "admin/product-form"; 
    }

    @PostMapping("/admin/products/save")
public String saveProduct(@Valid @ModelAttribute("product") Product product,
                          BindingResult result,
                          @RequestParam("imageFile") MultipartFile imageFile,
                          RedirectAttributes redirectAttributes,
                          Model model) {

    if (result.hasErrors()) {
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("pageTitle", (product.getId() == null) ? "Thêm sản phẩm mới" : "Chỉnh sửa sản phẩm");
        return "admin/product-form";
    }

    String uploadedFileName = null;
    if (!imageFile.isEmpty()) {
        try {
            uploadedFileName = saveUploadedFile(imageFile);
            product.setImageUrl("/uploads/" + uploadedFileName); // Đảm bảo đường dẫn chính xác
        } catch (IOException e) {
            System.err.println("Lỗi upload file: " + e.getMessage());
            model.addAttribute("errorMessage", "Lỗi khi tải lên hình ảnh: " + e.getMessage());
            model.addAttribute("categories", categoryService.findAllCategories());
            return "admin/product-form";
        }
    } else if (product.getId() != null && product.getImageUrl() == null) {
        Optional<Product> existingProductOpt = productService.findProductById(product.getId());
        existingProductOpt.ifPresent(existingProduct -> product.setImageUrl(existingProduct.getImageUrl()));
    }

    try {
        if (product.getCategory() != null && product.getCategory().getId() != null) {
            Category category = categoryService.findCategoryById(product.getCategory().getId())
                    .orElseThrow(() -> new RuntimeException("Invalid Category ID: " + product.getCategory().getId()));
            product.setCategory(category);
        }

        productService.saveProduct(product);
        redirectAttributes.addFlashAttribute("successMessage", "Lưu sản phẩm thành công!");
        return "redirect:/products";
    } catch (Exception e) {
        model.addAttribute("errorMessage", "Lỗi khi lưu sản phẩm: " + e.getMessage());
        model.addAttribute("categories", categoryService.findAllCategories());
        model.addAttribute("pageTitle", (product.getId() == null) ? "Thêm sản phẩm mới" : "Chỉnh sửa sản phẩm");
        if (uploadedFileName != null) {
            product.setImageUrl("/uploads/" + uploadedFileName);
        }
        return "admin/product-form";
    }
}
    @GetMapping("/uploads/{filename}")
@ResponseBody
public ResponseEntity<Resource> serveFile(@PathVariable String filename) {
    Path file = Paths.get(UPLOAD_DIR).resolve(filename);
    try {
        Resource resource = new UrlResource(file.toUri());
        if (resource.exists() || resource.isReadable()) {
            return ResponseEntity.ok().body(resource);
        } else {
            throw new RuntimeException("Không thể đọc file " + filename);
        }
    } catch (MalformedURLException e) {
        throw new RuntimeException("Không tìm thấy file " + filename, e);
    }
}


    private String saveUploadedFile(MultipartFile file) throws IOException {
        String originalFilename = StringUtils.cleanPath(file.getOriginalFilename());

        String uniqueFileName = UUID.randomUUID().toString() + "_" + originalFilename;
        Path uploadPath = Paths.get(UPLOAD_DIR);
        if (!Files.exists(uploadPath)) {
            Files.createDirectories(uploadPath);
        }
        Path filePath = uploadPath.resolve(uniqueFileName);
        try (InputStream inputStream = file.getInputStream()) {
            Files.copy(inputStream, filePath, StandardCopyOption.REPLACE_EXISTING);
        }

        return uniqueFileName;
    }

    @GetMapping("/admin/products/edit/{id}")
    public String showEditProductForm(@PathVariable("id") Long id, Model model, RedirectAttributes redirectAttributes) {
        Optional<Product> productOpt = productService.findProductById(id);
        if (productOpt.isPresent()) {
            model.addAttribute("product", productOpt.get());
            model.addAttribute("categories", categoryService.findAllCategories());
             model.addAttribute("pageTitle", "Chỉnh sửa sản phẩm");
            return "admin/product-form";
        } else {
            redirectAttributes.addFlashAttribute("errorMessage", "Không tìm thấy sản phẩm với ID: " + id);
            return "redirect:/products";
        }
    }

    @GetMapping("/admin/products/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, RedirectAttributes redirectAttributes) {
        try {
            Optional<Product> productOpt = productService.findProductById(id);
            String imageUrlToDelete = null;
            if (productOpt.isPresent() && productOpt.get().getImageUrl() != null) {
                 imageUrlToDelete = productOpt.get().getImageUrl();
            }
            productService.deleteProductById(id);
             if (imageUrlToDelete != null && !imageUrlToDelete.isBlank()) {
                 deleteProductImage(imageUrlToDelete);
             }
            redirectAttributes.addFlashAttribute("successMessage", "Xóa sản phẩm thành công!");
        } catch (Exception e) {
            // Log lỗi e.getMessage()
            redirectAttributes.addFlashAttribute("errorMessage", "Lỗi khi xóa sản phẩm: " + e.getMessage());
        }
        return "redirect:/products";
    }
    private void deleteProductImage(String imageUrl) {
        if (imageUrl == null || !imageUrl.startsWith("/uploads/")) {
            return; 
        }
        try {
            String filename = imageUrl.substring("/uploads/".length());
            Path filePath = Paths.get(UPLOAD_DIR).resolve(filename);
            Files.deleteIfExists(filePath);
             System.out.println("Deleted image file: " + filePath);
        } catch (IOException e) {
             System.err.println("Error deleting image file " + imageUrl + ": " + e.getMessage());
        }
    }

    @GetMapping("/api/products")
    @ResponseBody
    public ResponseEntity<List<ProductDto>> getAllProductsApi() {
        List<Product> products = productService.findAllProducts();
        List<ProductDto> productDtos = products.stream().map(product -> {
            ProductDto dto = new ProductDto();
            BeanUtils.copyProperties(product, dto);
            return dto;
        }).collect(Collectors.toList());
        return ResponseEntity.ok(productDtos);
    }
    @GetMapping("/api/products/{id}")
    @ResponseBody
    public ResponseEntity<ProductDto> getProductByIdApi(@PathVariable Long id) {
        return productService.findProductById(id)
                .map(product -> {
                    ProductDto dto = new ProductDto();
                    BeanUtils.copyProperties(product, dto);
                    return ResponseEntity.ok(dto);
                })
                .orElse(ResponseEntity.notFound().build());
    }
    @PostMapping("/api/admin/products")
    @ResponseBody
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> createProductApi(@Valid @RequestBody ProductDto productDto) {
        if (productDto.getId() != null) {
            return ResponseEntity.badRequest().build();
        }
        try {
            Product product = new Product();
            BeanUtils.copyProperties(productDto, product);
            Product savedProduct = productService.saveProduct(product);
            ProductDto savedDto = new ProductDto();
            BeanUtils.copyProperties(savedProduct, savedDto);
            return ResponseEntity.status(HttpStatus.CREATED).body(savedDto);
        } catch (Exception e) {
            // Log lỗi
            return ResponseEntity.internalServerError().build();
        }
    }
    @PutMapping("/api/admin/products/{id}")
    @ResponseBody
    // @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProductDto> updateProductApi(@PathVariable Long id, @Valid @RequestBody ProductDto productDto) {
         Optional<Product> existingProductOpt = productService.findProductById(id);
         if (!existingProductOpt.isPresent()) {
             return ResponseEntity.notFound().build();
         }

         try {
             Product existingProduct = existingProductOpt.get();
             BeanUtils.copyProperties(productDto, existingProduct, "id");
             Product updatedProduct = productService.saveProduct(existingProduct);
             ProductDto updatedDto = new ProductDto();
             BeanUtils.copyProperties(updatedProduct, updatedDto);
             return ResponseEntity.ok(updatedDto);
         } catch (Exception e) {
              // Log lỗi
             return ResponseEntity.internalServerError().build();
         }
    }

    @DeleteMapping("/api/admin/products/{id}")
    @ResponseBody
    public ResponseEntity<Void> deleteProductApi(@PathVariable Long id) {
        try {
            if (!productService.findProductById(id).isPresent()) {
                return ResponseEntity.notFound().build();
            }
            productService.deleteProductById(id);
            return ResponseEntity.noContent().build();
        } catch (Exception e) {
            return ResponseEntity.internalServerError().build();
        }
    }
}