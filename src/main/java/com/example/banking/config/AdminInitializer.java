package com.example.banking.config;

import com.example.banking.Entity.AdminClass;
import com.example.banking.Repository.AdminRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.time.LocalDateTime;
import java.util.Optional;

@Configuration
public class AdminInitializer {

    @Bean
    public CommandLineRunner initAdmin(AdminRepository adminRepository, PasswordEncoder passwordEncoder) {
        return args -> {
            Optional<AdminClass> adminOpt = adminRepository.findByUsername("admin");
            if (adminOpt.isEmpty()) {
                AdminClass admin = new AdminClass();
                admin.setUsername("admin");
                admin.setPassword(passwordEncoder.encode("admin")); // Password: admin
                admin.setFullName("Administrator");
                admin.setEmail("admin@banking.com");
                admin.setRole("ROLE_ADMIN");
                admin.setStatus("ACTIVE");
                admin.setCreatedAt(LocalDateTime.now());
                admin.setFaceRegistered(false);
                adminRepository.save(admin);
                System.out.println("====== ĐÃ TẠO TÀI KHOẢN ADMIN MẶC ĐỊNH (username: admin, password: admin) ======");
            } else {
                // Đảm bảo mật khẩu là 'admin' để dễ dàng đăng nhập nếu quên
                AdminClass admin = adminOpt.get();
                admin.setPassword(passwordEncoder.encode("admin"));
                admin.setStatus("ACTIVE");
                adminRepository.save(admin);
                System.out.println("====== ĐÃ CẬP NHẬT MẬT KHẨU ADMIN MẶC ĐỊNH (username: admin, password: admin) ======");
            }
        };
    }
}
