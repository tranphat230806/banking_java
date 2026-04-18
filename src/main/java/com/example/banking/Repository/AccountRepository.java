package com.example.banking.Repository;

import com.example.banking.Entity.AccountClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AccountRepository extends JpaRepository <AccountClass ,Integer>{
    Optional<AccountClass> findByCode(String code);
}
