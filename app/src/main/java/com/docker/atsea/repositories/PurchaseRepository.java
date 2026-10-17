package com.docker.atsea.repositories;

import java.util.List;
import com.docker.atsea.model.Purchase;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PurchaseRepository extends JpaRepository<Purchase, Long> {
    List<Purchase> findAllByOrderByPurchaseIdDesc();
    List<Purchase> findByCustomerIdOrderByPurchaseIdDesc(Long customerId);
}
