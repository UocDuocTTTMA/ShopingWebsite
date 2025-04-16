package com.shop.web.service;

import com.shop.web.dto.RegisterRequest;
import com.shop.web.model.Role;
import com.shop.web.model.User;
import com.shop.web.repository.RoleRepository;
import com.shop.web.repository.UserRepository;
import com.shop.web.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder; // Sẽ inject bean này từ SecurityConfig

    @Override
    @Transactional // Đảm bảo toàn vẹn dữ liệu
    public User registerNewUser(RegisterRequest registerRequest) {
        if (usernameExists(registerRequest.getUsername())) {
            throw new RuntimeException("Error: Username đã tồn tại!"); // Nên tạo Exception tùy chỉnh
        }
        if (emailExists(registerRequest.getEmail())) {
            throw new RuntimeException("Error: Email đã được sử dụng!"); // Nên tạo Exception tùy chỉnh
        }

        User user = new User();
        user.setUsername(registerRequest.getUsername());
        user.setEmail(registerRequest.getEmail());
        user.setPassword(passwordEncoder.encode(registerRequest.getPassword())); // Mã hóa mật khẩu
        user.setEnabled(true);

        // Gán role mặc định là ROLE_USER
        Role userRole = roleRepository.findByName("ROLE_USER")
                .orElseThrow(() -> new RuntimeException("Error: Role USER is not found.")); // Cần đảm bảo Role này tồn tại
        user.addRole(userRole);

        return userRepository.save(user);
    }

    @Override
    public Optional<User> findByUsername(String username) {
        return userRepository.findByUsername(username);
    }

    @Override
    public boolean usernameExists(String username) {
        return userRepository.existsByUsername(username);
    }

    @Override
    public boolean emailExists(String email) {
        return userRepository.existsByEmail(email);
    }
}