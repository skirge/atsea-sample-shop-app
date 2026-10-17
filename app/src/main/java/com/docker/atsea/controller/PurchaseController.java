package com.docker.atsea.controller;

import java.util.Collections;
import java.util.Map;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.RequestBody;
import com.docker.atsea.model.Purchase;
import com.docker.atsea.repositories.PurchaseRepository;
import com.docker.atsea.service.CustomerService;
import com.docker.atsea.model.Customer;
import io.jsonwebtoken.Claims;
import javax.servlet.http.HttpServletRequest;


@RestController
@RequestMapping(path = "/purchase/")
public class PurchaseController {

	private final PurchaseRepository purchases;
	private final CustomerService customers;

	public PurchaseController(PurchaseRepository purchases, CustomerService customers) {
		this.purchases = purchases;
		this.customers = customers;
	}
	
	public static final Logger logger = LoggerFactory.getLogger(PurchaseController.class);
	
	@RequestMapping(value="", method = RequestMethod.POST)
    public ResponseEntity<?> purchase(@RequestBody Purchase purchase, HttpServletRequest request) {
        Customer customer = currentCustomer(request);
        if (customer == null || !Boolean.TRUE.equals(customer.getEnabled())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        if (purchase.getProductsOrdered() == null || purchase.getProductsOrdered().isEmpty()
                || purchase.getProductsOrdered().entrySet().stream().anyMatch(item ->
                    item.getKey() == null || item.getKey() <= 0
                    || item.getValue() == null || item.getValue() <= 0)) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
        purchase.setCustomerId(customer.getCustomerId());
        purchases.save(purchase);
    	logger.info("Performing purchase");
    	
        Map<String, String> message = Collections.singletonMap("message",
                "Thank you for shopping @Sea! We're sending a confirmation email shortly and getting your order ready!");
        return new ResponseEntity<>(message, HttpStatus.OK);
    }

    @RequestMapping(value = "", method = RequestMethod.GET, produces = "application/json")
    public ResponseEntity<?> listPurchases(HttpServletRequest request) {
        Customer customer = currentCustomer(request);
        if (customer == null || !Boolean.TRUE.equals(customer.getEnabled())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }
        return ResponseEntity.ok(purchases.findByCustomerIdOrderByPurchaseIdDesc(customer.getCustomerId()));
    }

    private Customer currentCustomer(HttpServletRequest request) {
        Claims claims = (Claims) request.getAttribute("claims");
        return claims == null ? null : customers.findByUserName(claims.getSubject());
    }

}
