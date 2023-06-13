package com.docker.atsea.ws.customers;

import javax.jws.WebMethod;
import javax.jws.WebParam;
import javax.jws.WebResult;
import javax.jws.WebService;
import javax.xml.ws.RequestWrapper;
import javax.xml.ws.ResponseWrapper;

@WebService
public interface Customer {

    @WebMethod
    CustomerDTO findCustomer(String name);
}
