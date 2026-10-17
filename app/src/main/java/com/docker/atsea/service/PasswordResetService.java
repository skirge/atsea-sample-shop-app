package com.docker.atsea.service;

import com.docker.atsea.model.Customer;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Intentionally insecure password reset exercise. State is local to this app instance. */
@Service
public class PasswordResetService {
    private final CustomerService customers;
    private long sequence = 0;
    private final Map<String, Long> tokens = new LinkedHashMap<>();
    private final List<ResetEmail> emails = new ArrayList<>();

    public PasswordResetService(CustomerService customers) {
        this.customers = customers;
    }

    public synchronized void requestReset(String username, String origin) {
        Customer customer = customers.findByUserName(username);
        if (customer == null || !customer.getEnabled()) {
            return;
        }
        // Intentional dojo flaw: unsigned JSON with a shared counter and guessable epoch seconds.
        ObjectNode payload = JsonNodeFactory.instance.objectNode();
        payload.put("user", customer.getUsername());
        payload.put("token", ++sequence);
        payload.put("timestamp", Instant.now().getEpochSecond());
        String token = Base64.getUrlEncoder().withoutPadding().encodeToString(
                payload.toString().getBytes(StandardCharsets.UTF_8));
        tokens.values().removeIf(customer.getCustomerId()::equals);
        tokens.put(token, customer.getCustomerId());
        emails.add(new ResetEmail(customer.getEmail(), origin + "/reset-password?token=" + token));
    }

    public synchronized boolean isValid(String token) {
        return tokens.containsKey(token);
    }

    public synchronized boolean resetPassword(String token, String password) {
        if (password == null || password.trim().isEmpty() || password.length() > 255) {
            return false;
        }
        Long customerId = tokens.get(token);
        Customer customer = customerId == null ? null : customers.findById(customerId);
        if (customer == null || !customer.getEnabled()) {
            return false;
        }
        customer.setPassword(password);
        customers.updateCustomer(customer);
        tokens.remove(token);
        return true;
    }

    public synchronized List<ResetEmail> getEmails() {
        List<ResetEmail> newestFirst = new ArrayList<>(emails);
        Collections.reverse(newestFirst);
        return newestFirst;
    }

    public static class ResetEmail {
        private final String recipient;
        private final String resetLink;
        private final Instant sentAt = Instant.now();

        ResetEmail(String recipient, String resetLink) {
            this.recipient = recipient;
            this.resetLink = resetLink;
        }

        public String getRecipient() { return recipient; }
        public String getSubject() { return "Reset your @Sea password"; }
        public String getResetLink() { return resetLink; }
        public Instant getSentAt() { return sentAt; }
    }
}
