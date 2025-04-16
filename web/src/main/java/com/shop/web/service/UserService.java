package com.shop.web.service;

import com.shop.web.dto.RegisterRequest;
import com.shop.web.model.User;
import java.util.Optional;

public interface UserService {
    User registerNewUser(RegisterRequest registerRequest);
    Optional<User> findByUsername(String username);
    boolean usernameExists(String username);
    boolean emailExists(String email);
}
