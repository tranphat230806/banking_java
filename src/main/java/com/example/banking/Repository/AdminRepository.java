package com.example.banking.Repository;

import com.example.banking.Entity.AdminClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<AdminClass, Long> {
    Optional<AdminClass> findByUsername(String username);
}
