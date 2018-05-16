package com.docker.atsea.repositories;

import com.docker.atsea.model.Customer;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;

@Repository
@Transactional()
public class CustomerRepositoryImpl implements CustomerRepositoryCustom {

    @PersistenceContext
    EntityManager entityManager;

    @Override
    public Customer findByUserName(String userName) {
        javax.persistence.Query query = entityManager.createNativeQuery("SELECT em.* FROM customer as em " +
                "WHERE em.username LIKE '" + userName + "%'", Customer.class);
        if(query.getResultList().isEmpty())
            return null;
        return (Customer) query.getSingleResult();
    }

}