package com.example.banking.Repository;

import com.example.banking.Entity.UserClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<UserClass, Long> {
    Optional<UserClass> findByUsername(String username);

    Optional<UserClass> findByEmail(String email);
}
