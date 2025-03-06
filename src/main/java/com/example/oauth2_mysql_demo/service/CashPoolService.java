package com.example.oauth2_mysql_demo.service;

import com.example.oauth2_mysql_demo.entity.CashPool;
import com.example.oauth2_mysql_demo.entity.Transaction;
import com.example.oauth2_mysql_demo.entity.User;
import com.example.oauth2_mysql_demo.entity.WithdrawalRequest;
import com.example.oauth2_mysql_demo.repository.CashPoolRepository;
import com.example.oauth2_mysql_demo.repository.TransactionRepository;
import com.example.oauth2_mysql_demo.repository.UserRepository;
import com.example.oauth2_mysql_demo.repository.WithdrawalRequestRepository;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class CashPoolService {
    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TransactionRepository transactionRepository;

    @Autowired
    private WithdrawalRequestRepository withdrawalRequestRepository;

    @Autowired
    private CashPoolRepository cashPoolRepository;

    public void deposit(double amount) {
        if (amount < 200) {
            throw new IllegalArgumentException("Minimum 200 TL yatırmalısınız!");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        Transaction transaction = new Transaction();
        transaction.setUser(user);
        transaction.setType("DEPOSIT");
        transaction.setAmount(amount);
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        CashPool cashPool = cashPoolRepository.findById(1L)
                .orElseGet(() -> {
                    CashPool newCashPool = new CashPool();
                    return cashPoolRepository.save(newCashPool);
                });
        cashPool.setTotalBalance(cashPool.getTotalBalance() + amount);
        cashPool.setLastUpdated(LocalDateTime.now());
        cashPoolRepository.save(cashPool);
    }

    public void requestWithdrawal(double amount, String paymentMethod, LocalDateTime estimatedRefundDate, String reason) {
        if (amount <= 0) {
            throw new IllegalArgumentException("Geçersiz miktar!");
        }

        CashPool cashPool = cashPoolRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Cash pool bulunamadı"));
        if (amount > cashPool.getTotalBalance()) {
            throw new IllegalArgumentException("Yetersiz bakiye!");
        }

        if (!("PEYDERPEY".equals(paymentMethod) || "TEK_SEFER".equals(paymentMethod))) {
            throw new IllegalArgumentException("Geçersiz ödeme yöntemi!");
        }

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        String username = auth.getName();
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));

        WithdrawalRequest request = new WithdrawalRequest();
        request.setUser(user);
        request.setAmount(amount);
        request.setStatus("PENDING");
        request.setTimestamp(LocalDateTime.now());
        request.setPaymentMethod(paymentMethod);
        request.setEstimatedRefundDate(estimatedRefundDate);
        request.setReason(reason);
        withdrawalRequestRepository.save(request);
    }

    public void approveWithdrawal(Long requestId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Talep bulunamadı"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Bu talep zaten işlenmiş!");
        }

        CashPool cashPool = cashPoolRepository.findById(1L)
                .orElseThrow(() -> new RuntimeException("Cash pool bulunamadı"));
        if (request.getAmount() > cashPool.getTotalBalance()) {
            throw new IllegalStateException("Yetersiz bakiye!");
        }

        request.setStatus("APPROVED");
        withdrawalRequestRepository.save(request);

        Transaction transaction = new Transaction();
        transaction.setUser(request.getUser());
        transaction.setType("WITHDRAWAL");
        transaction.setAmount(request.getAmount());
        transaction.setTimestamp(LocalDateTime.now());
        transactionRepository.save(transaction);

        cashPool.setTotalBalance(cashPool.getTotalBalance() - request.getAmount());
        cashPool.setLastUpdated(LocalDateTime.now());
        cashPoolRepository.save(cashPool);
    }

    public void rejectWithdrawal(Long requestId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Talep bulunamadı"));

        if (!"PENDING".equals(request.getStatus())) {
            throw new IllegalStateException("Bu talep zaten işlenmiş!");
        }

        request.setStatus("REJECTED");
        withdrawalRequestRepository.save(request);
    }

    public double getTotalBalance() {
        CashPool cashPool = cashPoolRepository.findById(1L)
                .orElseGet(() -> {
                    CashPool newCashPool = new CashPool();
                    return cashPoolRepository.save(newCashPool);
                });
        return cashPool.getTotalBalance();
    }

    public List<Transaction> getTransactionHistory() {
        return transactionRepository.findAllByOrderByTimestampDesc();
    }

    public List<WithdrawalRequest> getPendingWithdrawalRequests() {
        return withdrawalRequestRepository.findByStatus("PENDING");
    }

    public List<WithdrawalRequest> getRejectedRequestsForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return withdrawalRequestRepository.findByStatus("REJECTED").stream()
                .filter(request -> request.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
    }

    public WithdrawalRequest getLatestApprovedRequestForUser(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        List<WithdrawalRequest> approvedRequests = withdrawalRequestRepository.findByStatus("APPROVED").stream()
                .filter(request -> request.getUser().getId().equals(user.getId()))
                .collect(Collectors.toList());
        return approvedRequests.stream()
                .max(Comparator.comparing(WithdrawalRequest::getTimestamp))
                .orElse(null);
    }

    public void markRejectedRequestsAsNotified(String username) {
        List<WithdrawalRequest> rejectedRequests = getRejectedRequestsForUser(username);
        for (WithdrawalRequest request : rejectedRequests) {
            if ("REJECTED".equals(request.getStatus())) {
                request.setStatus("REJECTED_NOTIFIED");
                withdrawalRequestRepository.save(request);
            }
        }
    }

    public void markApprovedRequestAsNotified(Long requestId) {
        WithdrawalRequest request = withdrawalRequestRepository.findById(requestId)
                .orElseThrow(() -> new RuntimeException("Talep bulunamadı"));
        if ("APPROVED".equals(request.getStatus())) {
            request.setStatus("APPROVED_NOTIFIED");
            withdrawalRequestRepository.save(request);
        }
    }

    public List<Transaction> getUserTransactions(String username) {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Kullanıcı bulunamadı"));
        return transactionRepository.findByUserIdOrderByTimestampDesc(user.getId());
    }
}
