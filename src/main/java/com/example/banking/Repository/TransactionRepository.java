package com.example.banking.Repository;

import com.example.banking.Entity.TransactionsClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TransactionRepository extends JpaRepository <TransactionsClass, Integer>{
}
