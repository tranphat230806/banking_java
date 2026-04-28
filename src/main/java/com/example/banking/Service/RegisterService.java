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

import java.math.BigDecimal;
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

    public UserClass registerUser(RegisterDTO registerDTO) {

        // 1. Kiểm tra username đã tồn tại hay chưa
        if (userRepository.findByUsername(registerDTO.getUsername()).isPresent()) {
            throw new RuntimeException("Username đã tồn tại");
        }

        // 2. Tạo User mới
        UserClass newUser = new UserClass();
        newUser.setUsername(registerDTO.getUsername());
        newUser.setFullName(registerDTO.getFullName());
        newUser.setEmail(registerDTO.getEmail());
        newUser.setPhone(registerDTO.getPhone());
        newUser.setAvatar(registerDTO.getAvatar());
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
