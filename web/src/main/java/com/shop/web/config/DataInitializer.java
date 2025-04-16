package com.shop.web.config;

import com.shop.web.model.Role;
import com.shop.web.model.User;
import com.shop.web.model.Category;
import com.shop.web.repository.CategoryRepository;
import com.shop.web.repository.RoleRepository;
import com.shop.web.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private CategoryRepository categoryRepository;

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        createRoleIfNotFound("ROLE_USER");
        createRoleIfNotFound("ROLE_ADMIN");
        createCategoryIfNotFound("Điện tử");
        createCategoryIfNotFound("Thời trang");
        createCategoryIfNotFound("Sách");
        createAdminUserIfNotFound();
    }

    private void createRoleIfNotFound(String roleName) {
        Optional<Role> roleOpt = roleRepository.findByName(roleName);
        if (!roleOpt.isPresent()) {
            roleRepository.save(new Role(roleName));
            System.out.println("Created role: " + roleName);
        }
    }

     private void createAdminUserIfNotFound() {
        final String adminUsername = "admin";
        if (!userRepository.existsByUsername(adminUsername)) {
            User adminUser = new User();
            adminUser.setUsername(adminUsername);
            adminUser.setEmail("admin@shop.com");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEnabled(true);

            Role adminRole = roleRepository.findByName("ROLE_ADMIN")
                    .orElseThrow(() -> new RuntimeException("Error: Role ADMIN is not found."));
            Role userRole = roleRepository.findByName("ROLE_USER")
                     .orElseThrow(() -> new RuntimeException("Error: Role USER is not found."));

            adminUser.addRole(adminRole);
            adminUser.addRole(userRole);

            userRepository.save(adminUser);
             System.out.println("Created default admin user: " + adminUsername);
        }
    }

     private void createUserIfNotFound(String username, String email, String password, String roleName) {
        if (!userRepository.existsByUsername(username)) {
            User user = new User();
            user.setUsername(username);
            user.setEmail(email);
            user.setPassword(passwordEncoder.encode(password));
            user.setEnabled(true);

            Role role = roleRepository.findByName(roleName)
                    .orElseThrow(() -> new RuntimeException("Error: Role " + roleName + " is not found."));
            user.addRole(role);

            userRepository.save(user);
            System.out.println("Created default user: " + username);
        }
    }
    private void createCategoryIfNotFound(String categoryName) {
        if (!categoryRepository.existsByName(categoryName)) {
            categoryRepository.save(new Category(categoryName));
            System.out.println("Created category: " + categoryName);
        }
    }
}