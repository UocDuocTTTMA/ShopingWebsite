package com.shop.web.controllers;

import com.shop.web.dto.RegisterRequest;
import com.shop.web.model.User;
import com.shop.web.service.UserService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class AuthController {

    @Autowired
    private UserService userService;

    @GetMapping("/login")
    public String showLoginPage() {
        // Trả về tên của view template login (không cần .html)
        return "auth/login";
    }

    @GetMapping("/register")
    public String showRegistrationForm(Model model) {
        // Đưa một đối tượng RegisterRequest rỗng vào model để form có thể binding
        model.addAttribute("user", new RegisterRequest());
        return "auth/register";
    }

    @PostMapping("/register")
    public String processRegistration(@Valid @ModelAttribute("user") RegisterRequest registerRequest,
                                      BindingResult result, // Kết quả validation
                                      Model model,
                                      RedirectAttributes redirectAttributes) {

        // Kiểm tra validation cơ bản (NotBlank, Email, Size...)
        if (result.hasErrors()) {
            // Nếu có lỗi, quay lại form đăng ký và hiển thị lỗi
            return "auth/register";
        }

        // Kiểm tra logic nghiệp vụ (username/email đã tồn tại chưa)
        if (userService.usernameExists(registerRequest.getUsername())) {
            result.rejectValue("username", "error.user", "Username đã tồn tại");
        }
        if (userService.emailExists(registerRequest.getEmail())) {
            result.rejectValue("email", "error.user", "Email đã được sử dụng");
        }

        if (result.hasErrors()) {
            return "auth/register"; // Quay lại form nếu có lỗi nghiệp vụ
        }

        try {
            userService.registerNewUser(registerRequest);
            // Thêm flash attribute để hiển thị thông báo thành công sau khi redirect
            redirectAttributes.addFlashAttribute("successMessage", "Đăng ký thành công! Vui lòng đăng nhập.");
            return "redirect:/login"; // Chuyển hướng đến trang đăng nhập
        } catch (RuntimeException e) {
            // Xử lý lỗi nếu có vấn đề khi lưu user (ví dụ: lỗi DB)
            // Ghi log lỗi e.getMessage()
            model.addAttribute("errorMessage", "Đã xảy ra lỗi trong quá trình đăng ký. Vui lòng thử lại.");
            // Đưa lại thông tin user đã nhập vào form (trừ password)
            registerRequest.setPassword(""); // Xóa password đi
            model.addAttribute("user", registerRequest);
            return "auth/register";
        }
    }
}