package com.example.oauth2_mysql_demo.repository;

import com.example.oauth2_mysql_demo.entity.WithdrawalRequest;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WithdrawalRequestRepository extends JpaRepository<WithdrawalRequest, Long> {
    List<WithdrawalRequest> findByStatus(String status);
}
