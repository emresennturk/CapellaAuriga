package com.example.oauth2_mysql_demo.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Table(name = "withdrawal_requests")
@Data
@Getter
@Setter
public class WithdrawalRequest {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private double amount;

    @Column(nullable = false)
    private String status; // "PENDING", "APPROVED", "REJECTED"

    @Column(nullable = false)
    private LocalDateTime timestamp;

    @Column(nullable = true)
    private String paymentMethod; // "PEYDERPEY" veya "TEK_SEFER"

    @Column(nullable = true)
    private LocalDateTime estimatedRefundDate; // Tahmini geri ödeme tarihi

    @Column(nullable = true, length = 500)
    private String reason; // Çekim sebebi
}
