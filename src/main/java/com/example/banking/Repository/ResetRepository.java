package com.example.banking.Repository;

import com.example.banking.Entity.ResetPasswordClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ResetRepository extends JpaRepository<ResetPasswordClass, Long> {
    Optional<ResetPasswordClass> findByCodeOTPAndIsUsedFalse(String otp);
}
