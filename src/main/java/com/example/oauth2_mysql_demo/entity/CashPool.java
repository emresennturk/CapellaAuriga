package com.example.oauth2_mysql_demo.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "cash_pool")
@Data
@Getter
@Setter
public class CashPool {

    @Id
    private Long id = 1L; // Tek bir kayıt tutacağımız için sabit ID (1)

    private double totalBalance;

    private LocalDateTime lastUpdated;

    // Varsayılan constructor
    public CashPool() {
        this.totalBalance = 0.0;
        this.lastUpdated = LocalDateTime.now();
    }
}
