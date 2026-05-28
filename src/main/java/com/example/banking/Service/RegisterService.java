package com.example.banking.Service;

import com.example.banking.DTO.RegisterDTO;
import com.example.banking.Entity.AccountClass;
import com.example.banking.Entity.UserClass;
import com.example.banking.Repository.AccountRepository;
import com.example.banking.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Random;

@Service
public class RegisterService {

    @Autowired
    private UserRepository userRepository;  // Dùng repository để lưu User

    @Autowired
    private PasswordEncoder passwordEncoder;  // Dùng để mã hóa mật khẩu

    @Autowired
    private AccountRepository accountRepository;

    @Transactional
    public boolean isUsernameTaken(String username) {
        return userRepository.findByUsername(username).isPresent();
    }

    public UserClass registerUser(RegisterDTO registerDTO) throws IOException {

        // 1. Kiểm tra username đã tồn tại hay chưa
        if (userRepository.findByUsername(registerDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }

        // 2. Xử lý avatar: lưu file vào disk, lấy đường dẫn
        String avatarPath = null;
        MultipartFile avatarFile = registerDTO.getAvatar();
        if (avatarFile != null && !avatarFile.isEmpty()) {
            String contentType = avatarFile.getContentType();
            if (contentType == null || !contentType.startsWith("image/")) {
                throw new RuntimeException("Chỉ được upload file ảnh.");
            }
            String originalName = avatarFile.getOriginalFilename();
            String extension = (originalName != null && originalName.contains("."))
                    ? originalName.substring(originalName.lastIndexOf("."))
                    : ".jpg";
            String filename = registerDTO.getUsername() + "_" + System.currentTimeMillis() + extension;
            Path uploadPath = Paths.get("uploads/avatars");
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(filename);
            avatarFile.transferTo(filePath);
            avatarPath = "/uploads/avatars/" + filename;
        }

        // 3. Tạo User mới
        UserClass newUser = new UserClass();
        newUser.setUsername(registerDTO.getUsername());
        newUser.setFullName(registerDTO.getFullName());
        newUser.setEmail(registerDTO.getEmail());
        newUser.setPhone(registerDTO.getPhone());
        newUser.setAvatar(avatarPath);
        newUser.setCreatedAt(LocalDateTime.now());
        // Mã hóa password trước khi lưu vào DB
        newUser.setPassword(passwordEncoder.encode(registerDTO.getPassword()));
        // 3. Lưu User vào database
        UserClass saveUser = userRepository.save(newUser);

        AccountClass acc = new AccountClass();
        acc.setUser(saveUser);
        acc.setBalance(BigDecimal.valueOf(0));
        acc.setCurrency("VND");
        acc.setCode(generateAccountNumber());
        accountRepository.save(acc);
        return saveUser;
    }

    private String generateAccountNumber() {
        Random random = new Random();
        return String.valueOf(100000000 + random.nextInt(900000000));
    }
}
