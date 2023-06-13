package com.docker.atsea.ws.customers;

import com.docker.atsea.model.Customer;
import com.docker.atsea.ws.students.Student;
import com.docker.atsea.ws.students.StudentImpl;

import javax.xml.bind.annotation.adapters.XmlAdapter;

public class CustomerDTOAdapter extends XmlAdapter<CustomerDTOImpl, CustomerDTO> {

    @Override
    public CustomerDTO unmarshal(CustomerDTOImpl v) throws Exception {
        return v;
    }

    @Override
    public CustomerDTOImpl marshal(CustomerDTO v) throws Exception {
        if (v instanceof CustomerDTOImpl) {
            return (CustomerDTOImpl) v;
        }
        return new CustomerDTOImpl(v.getCustomerId(),v.getName(),v.getAddress(),v.getEmail(),v.getPhone(),
                v.getUsername(),v.getPassword(), v.getEnabled(),v.getRole());
    }
}
