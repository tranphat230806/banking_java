package com.example.banking.config;

import com.example.banking.Entity.AdminClass;
import com.example.banking.Repository.AdminRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * Tự động tạo tài khoản admin mặc định khi khởi động app lần đầu.
 * Chỉ chạy một lần — nếu admin đã tồn tại thì bỏ qua.
 */
@Component
public class DataInitializer implements ApplicationRunner {

    @Autowired
    private AdminRepository adminRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(ApplicationArguments args) {
        // Chỉ tạo nếu chưa có admin nào
        if (adminRepository.findByUsername("admin").isEmpty()) {
            AdminClass admin = new AdminClass();
            admin.setUsername("admin");
            admin.setPassword(passwordEncoder.encode("admin123")); // Hash đúng bằng BCrypt
            admin.setFullName("Administrator");
            admin.setEmail("admin@banking.com");
            admin.setRole("ROLE_ADMIN");
            admin.setStatus("ACTIVE");
            admin.setCreatedAt(LocalDateTime.now());
            admin.setFaceRegistered(false);

            adminRepository.save(admin);
            System.out.println(">>> [DataInitializer] Đã tạo admin mặc định: username=admin / password=admin123");
        } else {
            System.out.println(">>> [DataInitializer] Admin đã tồn tại, bỏ qua khởi tạo.");
        }
    }
}
