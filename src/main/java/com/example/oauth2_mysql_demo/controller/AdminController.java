package com.example.oauth2_mysql_demo.controller;

import com.example.oauth2_mysql_demo.service.CashPoolService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
@RequestMapping("/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {
    @Autowired
    private CashPoolService cashPoolService;

    @GetMapping
    public String adminPanel(Model model) {
        model.addAttribute("totalBalance", cashPoolService.getTotalBalance());
        model.addAttribute("withdrawalRequests", cashPoolService.getPendingWithdrawalRequests());
        model.addAttribute("transactions", cashPoolService.getTransactionHistory());
        return "admin";
    }

    @PostMapping("/approve-withdrawal")
    public String approveWithdrawal(@RequestParam("requestId") Long requestId) {
        cashPoolService.approveWithdrawal(requestId);
        return "redirect:/admin";
    }

    @PostMapping("/reject-withdrawal")
    public String rejectWithdrawal(@RequestParam("requestId") Long requestId) {
        cashPoolService.rejectWithdrawal(requestId);
        return "redirect:/admin";
    }
}
