package com.docker.atsea.controller;

import com.docker.atsea.repositories.PurchaseRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class AdminPurchaseController {
    private final PurchaseRepository purchases;

    public AdminPurchaseController(PurchaseRepository purchases) {
        this.purchases = purchases;
    }

    @GetMapping(value = {"/admin/purchases", "/admin/purchases/"}, produces = "text/html")
    public String listPurchases(Model model) {
        model.addAttribute("purchases", purchases.findAllByOrderByPurchaseIdDesc());
        return "admin/purchases";
    }
}
