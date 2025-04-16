package com.shop.web.config;

import com.shop.web.service.UserDetailsServiceImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer; // Cho disable CSRF nếu cần
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.AntPathRequestMatcher;

@Configuration
@EnableWebSecurity // Bật tính năng Security của Spring Web
public class SecurityConfig {

    // Inject UserDetailsService tùy chỉnh
    private final UserDetailsServiceImpl userDetailsService;

    public SecurityConfig(UserDetailsServiceImpl userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // Bean mã hóa mật khẩu
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // Bean cấu hình chính của Spring Security (thay thế configure(HttpSecurity http))
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers("/login", "/register", "/css/**", "/js/**", "/images/**", "/uploads/**", "/", "/home", "/products/**", "/api/products/**").permitAll()
                        .requestMatchers("/products/**", "/api/products/**").permitAll()
                        .requestMatchers("/admin/**", "/api/admin/**").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // Đường dẫn đến trang login tùy chỉnh
                        .loginProcessingUrl("/login") // URL xử lý submit form login (Spring Security tự động xử lý)
                        .defaultSuccessUrl("/home", true) // Chuyển hướng sau khi login thành công
                        .failureUrl("/login?error=true") // Chuyển hướng khi login thất bại
                        .permitAll() // Cho phép tất cả truy cập trang login
                )
                .logout(logout -> logout
                        .logoutRequestMatcher(new AntPathRequestMatcher("/logout")) // URL để logout
                        .logoutSuccessUrl("/login?logout=true") // Chuyển hướng sau khi logout
                        .invalidateHttpSession(true) // Hủy session
                        .deleteCookies("JSESSIONID") // Xóa cookie (tùy chọn)
                        .permitAll() // Cho phép tất cả thực hiện logout
                );
                // .csrf(AbstractHttpConfigurer::disable); // Tạm thời tắt CSRF cho API nếu cần (Không khuyến khích cho production nếu không có giải pháp thay thế như JWT)
                 // Nếu dùng form Thymeleaf thì nên để CSRF bật (mặc định là bật)

        return http.build();
    }

    // Cấu hình AuthenticationManager để sử dụng UserDetailsService và PasswordEncoder
    @Bean
    public AuthenticationManager authenticationManager(HttpSecurity http) throws Exception {
         AuthenticationManagerBuilder authenticationManagerBuilder =
             http.getSharedObject(AuthenticationManagerBuilder.class);
         authenticationManagerBuilder
             .userDetailsService(userDetailsService)
             .passwordEncoder(passwordEncoder());
         return authenticationManagerBuilder.build();
    }

    /* Cấu hình bỏ qua security cho static resources (cách khác thay vì dùng requestMatchers)
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.ignoring().requestMatchers("/css/**", "/js/**", "/images/**");
    }
    */
}