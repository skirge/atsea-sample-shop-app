package com.docker.atsea.configuration;

import com.docker.atsea.model.Customer;
import com.docker.atsea.service.CustomerService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class AdminAccountInitializer implements CommandLineRunner {
    private final CustomerService customers;

    public AdminAccountInitializer(CustomerService customers) {
        this.customers = customers;
    }

    @Override
    public void run(String... args) {
        if (customers.findByUserName("admin") == null) {
            customers.createCustomer(new Customer(null, "Dojo admin", "Dojo",
                    "admin@example.test", "0000000000", "admin", "admin123!", true, "ADMIN"));
        }
    }
}
