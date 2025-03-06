package com.example.oauth2_mysql_demo.controller;

import com.example.oauth2_mysql_demo.entity.Transaction;
import com.example.oauth2_mysql_demo.entity.WithdrawalRequest;
import com.example.oauth2_mysql_demo.service.CashPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Controller
@RequestMapping("/api")
public class ResourceController {

    @Autowired
    private CashPoolService cashPoolService;

    @GetMapping("/hello")
    public String hello(Model model, Authentication authentication) {
        String username = authentication.getName();
        model.addAttribute("message", "Merhaba, " + username + "!");
        model.addAttribute("totalBalance", cashPoolService.getTotalBalance());
        boolean isAdmin = authentication.getAuthorities().contains(new SimpleGrantedAuthority("ROLE_ADMIN"));
        model.addAttribute("isAdmin", isAdmin);

        // Reddedilen talepleri al
        List<WithdrawalRequest> rejectedRequests = cashPoolService.getRejectedRequestsForUser(username);
        model.addAttribute("rejectedRequests", rejectedRequests);
        if (!rejectedRequests.isEmpty()) {
            cashPoolService.markRejectedRequestsAsNotified(username);
        }

        // En son onaylanan talebi al (notified olmadan önce)
        WithdrawalRequest latestApprovedRequest = cashPoolService.getLatestApprovedRequestForUser(username);
        model.addAttribute("latestApprovedRequest", latestApprovedRequest);

        // Kullanıcının işlemlerini al
        List<Transaction> userTransactions = cashPoolService.getUserTransactions(username);
        model.addAttribute("userTransactions", userTransactions);

        return "hello";
    }

    @PostMapping("/deposit")
    public String deposit(@RequestParam("amount") double amount, Model model) {
        try {
            cashPoolService.deposit(amount);
            return "redirect:/api/hello";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("totalBalance", cashPoolService.getTotalBalance());
            return "hello";
        }
    }

    @GetMapping("/request-withdrawal-form")
    public String showWithdrawalForm(Model model) {
        return "withdrawal-form";
    }

    @PostMapping("/request-withdrawal")
    public String requestWithdrawal(
            @RequestParam("amount") double amount,
            @RequestParam("paymentMethod") String paymentMethod,
            @RequestParam("estimatedRefundDate") String estimatedRefundDate,
            @RequestParam("reason") String reason,
            Model model) {
        try {
            LocalDateTime refundDate = LocalDateTime.parse(estimatedRefundDate);
            cashPoolService.requestWithdrawal(amount, paymentMethod, refundDate, reason);
            return "redirect:/api/hello";
        } catch (IllegalArgumentException e) {
            model.addAttribute("error", e.getMessage());
            model.addAttribute("totalBalance", cashPoolService.getTotalBalance());
            return "hello";
        } catch (Exception e) {
            model.addAttribute("error", "Geçersiz tarih formatı veya hata: " + e.getMessage());
            model.addAttribute("totalBalance", cashPoolService.getTotalBalance());
            return "hello";
        }
    }

    @PostMapping("/mark-approved-notified")
    @ResponseBody
    public String markApprovedNotified(@RequestBody Map<String, Long> payload) {
        Long requestId = payload.get("requestId");
        if (requestId != null) {
            cashPoolService.markApprovedRequestAsNotified(requestId);
            return "Success";
        }
        return "Failure";
    }

    @GetMapping("/other-endpoint")
    public String testJwtEndpoint(Model model, Authentication authentication) {
        model.addAttribute("message", "Bu bir JWT test endpoint'idir. Erişim başarılı!");
        model.addAttribute("username", authentication.getName());
        return "test-jwt";
    }
}
