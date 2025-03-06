package com.example.oauth2_mysql_demo.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class LoginController {
    @Autowired
    private AuthenticationManager authenticationManager;

    @GetMapping("/custom-login")
    public String showLoginPage(Model model, @RequestParam(value = "error", required = false) String error,
                                @RequestParam(value = "logout", required = false) String logout,
                                HttpSession session) {
        if (error != null) {
            model.addAttribute("error", "Kullanıcı adı veya şifre yanlış!");
        }
        if (logout != null) {
            model.addAttribute("logout", true);
            session.invalidate(); // Çıkış yapıldığında oturumu temizle
        }
        return "login";
    }

    @PostMapping("/custom-login")
    public String processLogin(@RequestParam("username") String username,
                               @RequestParam("password") String password,
                               Model model,
                               HttpServletRequest request) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(username, password)
            );
            SecurityContextHolder.getContext().setAuthentication(authentication);
            // Oturumu yenile ve kullanıcıyı yönlendir
            request.getSession().setAttribute("SPRING_SECURITY_CONTEXT", SecurityContextHolder.getContext());
            return "redirect:/api/hello";
        } catch (AuthenticationException e) {
            model.addAttribute("error", "Kullanıcı adı veya şifre yanlış!");
            return "login";
        }
    }
}
