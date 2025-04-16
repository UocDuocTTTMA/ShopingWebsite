package com.shop.web.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "roles")
@Getter
@Setter
@NoArgsConstructor
public class Role {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(length = 40, nullable = false, unique = true)
    private String name; // Ví dụ: ROLE_USER, ROLE_ADMIN

    public Role(String name) {
        this.name = name;
    }

    // toString() để dễ debug
    @Override
    public String toString() {
        return this.name;
    }
}