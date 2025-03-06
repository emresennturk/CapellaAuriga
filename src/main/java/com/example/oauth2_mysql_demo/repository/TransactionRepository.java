package com.example.oauth2_mysql_demo.repository;

import com.example.oauth2_mysql_demo.entity.Transaction;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface TransactionRepository extends JpaRepository<Transaction, Long> {
    List<Transaction> findAllByOrderByTimestampDesc();
    List<Transaction> findByUserIdOrderByTimestampDesc(Long userId);
}
