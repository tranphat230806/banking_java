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
    //send OTP to user
    public void guiOTP(String email) {
        UserClass user = userepo.findByEmail(email).orElseThrow(() -> new RuntimeException("không tìm thấy email này trong hệ thống"));
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        ResetPasswordClass reset = new ResetPasswordClass();
        reset.setUser(user);
        reset.setCodeOTP(otp);
        reset.setExpiredAt(LocalDateTime.now().plusMinutes(5));

        resetrepo.save(reset);

        // gửi mail
        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(email);
        msg.setSubject("OTP Reset Password");
        msg.setText("Mã OTP của bạn: " + otp + " (hết hạn 5 phút)");
        mailSender.send(msg);
    }

    public void resetPassword(String otp, String newPassword) {

        ResetPasswordClass reset = resetrepo.findByCodeOTPAndIsUsedFalse(otp).orElseThrow(() -> new RuntimeException("OTP sai"));

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
