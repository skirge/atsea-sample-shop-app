package com.docker.atsea.ws.customers;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlJavaTypeAdapter(CustomerDTOAdapter.class)
public interface CustomerDTO {
    public Long getCustomerId() ;

    public String getName();

    public String getAddress();

    public String getEmail() ;

    public String getPhone();

    public String getUsername();

    public String getPassword() ;

    public Boolean getEnabled();

    public String getRole() ;
}
