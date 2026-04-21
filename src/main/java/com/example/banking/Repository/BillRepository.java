package com.example.banking.Repository;

import com.example.banking.Entity.BillClass;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BillRepository extends JpaRepository<BillClass, Integer> {
}
