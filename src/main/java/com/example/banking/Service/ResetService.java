package com.example.banking.Service;

import com.example.banking.Entity.ResetPasswordClass;
import com.example.banking.Entity.UserClass;
import com.example.banking.Repository.ResetRepository;
import com.example.banking.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Transactional
@Service
public class ResetService {
    @Autowired
    UserRepository userepo;
    @Autowired
    ResetRepository resetrepo;
    @Autowired
    JavaMailSender mailSender;
    @Autowired
    PasswordEncoder passwordEncoder;

    @Transactional
    public void guiOTP(String username, String email) {
        // Tìm user theo username trước
        UserClass user = userepo.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Không tìm thấy tài khoản với username này"));

        // Kiểm tra email có khớp với tài khoản đó không
        if (!user.getEmail().equalsIgnoreCase(email.trim())) {
            throw new RuntimeException("Email không khớp với tài khoản đã đăng ký");
        }

        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        ResetPasswordClass reset = new ResetPasswordClass();
        reset.setUser(user);
        reset.setCodeOTP(otp);
        reset.setExpiredAt(LocalDateTime.now().plusMinutes(5));
        resetrepo.save(reset);

        // Gửi mail
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("OTP Reset Password");
        msg.setText("Mã OTP của " + user.getUsername() + " là: " + otp + " (hết hạn 5 phút)");
        mailSender.send(msg);
    }

    public void resetPassword(String otp, String newPassword) {

        ResetPasswordClass reset = resetrepo.findByCodeOTPAndIsUsedFalse(otp)
                .orElseThrow(() -> new RuntimeException("OTP sai"));

        if (reset.getExpiredAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP hết hạn");
        }

        UserClass user = reset.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userepo.save(user);

        reset.setUsed(true);
        resetrepo.save(reset);
    }
}
