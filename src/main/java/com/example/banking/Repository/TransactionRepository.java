package com.example.banking.Repository;

import com.example.banking.Entity.AccountClass;
import com.example.banking.Entity.TransactionsClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionsClass, Integer> {
    List<TransactionsClass> findTop5ByFromAccountOrderByDateDesc(AccountClass acc);
}
