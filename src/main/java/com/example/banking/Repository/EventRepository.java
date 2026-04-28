package com.example.banking.Repository;

import com.example.banking.Entity.EventClass;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface EventRepository extends JpaRepository<EventClass, Long> {
    List<EventClass> findByStatusTrueOrderByDisLayOrDerAsc();
}
