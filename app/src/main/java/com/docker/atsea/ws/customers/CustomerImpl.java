package com.docker.atsea.ws.customers;

import com.docker.atsea.service.CustomerService;
import java.util.logging.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;
import org.springframework.web.context.support.SpringBeanAutowiringSupport;

import javax.jws.WebService;

@WebService(endpointInterface = "com.docker.atsea.ws.customers.Customer")
@Component
public class CustomerImpl extends SpringBeanAutowiringSupport implements Customer  {

    private static final Logger LOG = Logger.getLogger(CustomerImpl.class.getName());
    @Autowired
    CustomerService customerService;

    @Override
    public CustomerDTO findCustomer(String name) {
        LOG.info("Searching for customer = [" + name + "]");
        Object c = customerService.findByUserName(name);
        if (c == null) {
            CustomerDTOImpl dto = new CustomerDTOImpl();
            dto.setStatus("NOT_FOUND");
            return dto;
        }
        LOG.info("Found customer = [" + c + "]");
        return new CustomerDTOImpl((com.docker.atsea.model.Customer) c);
    }

}
