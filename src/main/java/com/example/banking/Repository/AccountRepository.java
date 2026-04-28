package com.example.banking.Repository;

import com.example.banking.Entity.AccountClass;
import com.example.banking.Entity.UserClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository<AccountClass, Long> {
    Optional<AccountClass> findByCode(String code);

    Optional<AccountClass> findByUserId(long user);
}