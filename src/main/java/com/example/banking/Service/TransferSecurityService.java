package com.example.banking.Service;

import com.example.banking.Entity.UserClass;
import com.example.banking.Repository.UserRepository;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Random;

@Service
@Transactional
public class TransferSecurityService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JavaMailSender mailSender;

    public void setupPin(UserClass user, String pin) {
        user.setTransferPin(passwordEncoder.encode(pin));
        user.setPinFailCount(0);
        user.setTransferLocked(false);
        userRepository.save(user);
    }

    public boolean isPinSetup(UserClass user) {
        return user.getTransferPin() != null && !user.getTransferPin().isEmpty();
    }

    public boolean verifyPin(UserClass user, String rawPin) {
        if (user.isTransferLocked()) {
            return false;
        }

        if (passwordEncoder.matches(rawPin, user.getTransferPin())) {
            // Success, reset counter
            user.setPinFailCount(0);
            userRepository.save(user);
            return true;
        } else {
            // Failed
            user.setPinFailCount(user.getPinFailCount() + 1);
            if (user.getPinFailCount() >= 5) {
                user.setTransferLocked(true);
                sendUnlockOtp(user);
            }
            userRepository.save(user);
            return false;
        }
    }

    public void sendUnlockOtp(UserClass user) {
        String otp = String.valueOf(100000 + new Random().nextInt(900000));
        user.setTransferOtp(otp);
        user.setTransferOtpExpiry(LocalDateTime.now().plusMinutes(5));
        userRepository.save(user);

        SimpleMailMessage msg = new SimpleMailMessage();
        msg.setTo(user.getEmail());
        msg.setSubject("OTP Unlock Transfer PIN");
        msg.setText("Tài khoản của bạn đã bị khóa tính năng chuyển tiền do nhập sai PIN nhiều lần.\n" +
                "Mã OTP để mở khóa của bạn là: " + otp + "\n" +
                "(Mã này có hiệu lực trong 5 phút)");
        mailSender.send(msg);
    }

    public boolean unlockAccount(UserClass user, String otp) {
        if (!user.isTransferLocked() || user.getTransferOtp() == null) {
            return false; // Not locked or no OTP
        }

        if (user.getTransferOtpExpiry().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("OTP đã hết hạn. Vui lòng yêu cầu gửi lại OTP.");
        }

        if (user.getTransferOtp().equals(otp)) {
            user.setTransferLocked(false);
            user.setPinFailCount(0);
            user.setTransferOtp(null);
            user.setTransferOtpExpiry(null);
            userRepository.save(user);
            return true;
        }
        return false;
    }
}
