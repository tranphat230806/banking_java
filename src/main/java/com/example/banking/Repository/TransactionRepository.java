package com.example.banking.Repository;

import com.example.banking.Entity.AccountClass;
import com.example.banking.Entity.TransactionsClass;
import com.example.banking.Entity.UserClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<TransactionsClass, Long> {
    List<TransactionsClass> findTTop5ByFromAccountOrderByCreatedDesc(AccountClass fromAccount);
}
