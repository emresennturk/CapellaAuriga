package com.example.oauth2_mysql_demo.repository;

import com.example.oauth2_mysql_demo.entity.CashPool;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CashPoolRepository extends JpaRepository<CashPool, Long> {
}
