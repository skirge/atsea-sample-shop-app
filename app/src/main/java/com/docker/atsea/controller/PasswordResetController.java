package com.docker.atsea.controller;

import com.docker.atsea.service.PasswordResetService;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class PasswordResetController {
    private final PasswordResetService resets;

    public PasswordResetController(PasswordResetService resets) {
        this.resets = resets;
    }

    @GetMapping(value = "/forgot-password", produces = "text/html")
    public String forgotPassword() {
        return "forgot-password";
    }

    @PostMapping(value = "/forgot-password", produces = "text/html")
    public String requestReset(@RequestParam String username, HttpServletRequest request, Model model) {
        // Intentional dojo flaw: trust the caller's Host header when building the email link.
        String origin = request.getScheme() + "://" + request.getHeader("Host") + request.getContextPath();
        resets.requestReset(username, origin);
        model.addAttribute("message", "If that account exists, a password reset email has been sent.");
        return "forgot-password";
    }

    @GetMapping(value = "/reset-password", produces = "text/html")
    public String resetForm(@RequestParam(defaultValue = "") String token, Model model,
                            HttpServletResponse response) {
        model.addAttribute("token", token);
        if (!resets.isValid(token)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("error", "Invalid or used reset token. Request a new reset email.");
        }
        return "reset-password";
    }

    @PostMapping(value = "/reset-password", produces = "text/html")
    public String resetPassword(@RequestParam String token, @RequestParam String password,
                                Model model, HttpServletResponse response) {
        model.addAttribute("token", token);
        if (!resets.resetPassword(token, password)) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            model.addAttribute("error", "Use a valid, unused token and a password of 1 to 255 characters.");
        } else {
            model.addAttribute("message", "Password changed. You can now sign in with your new password.");
        }
        return "reset-password";
    }

    @GetMapping(value = {"/admin/emails", "/admin/emails/"}, produces = "text/html")
    public String emails(Model model) {
        model.addAttribute("emails", resets.getEmails());
        return "admin/emails";
    }
}
