package com.docker.atsea.repositories;

import com.docker.atsea.model.Customer;
import org.springframework.data.repository.query.Param;

public interface CustomerRepositoryCustom {
    Customer findByUserName(@Param("userName") String userName);
}
