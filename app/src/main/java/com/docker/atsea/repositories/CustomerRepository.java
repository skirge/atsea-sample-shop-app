package com.docker.atsea.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import com.docker.atsea.model.Customer;

@Repository
public interface CustomerRepository extends JpaRepository<Customer, Long>, CustomerRepositoryCustom {

    //TODO make it vulnerable to SQLi
	//Customer findOne(Long customerId);

	//TODO make it vulnerable to SQLi
	Customer findByName(String name);

}

