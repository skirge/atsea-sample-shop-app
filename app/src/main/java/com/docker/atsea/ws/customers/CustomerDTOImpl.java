package com.docker.atsea.ws.customers;

import javax.xml.bind.annotation.XmlType;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.Objects;

@XmlType(name = "Customer")
public class CustomerDTOImpl implements CustomerDTO {

    private Long customerId;
    private String name;
    private String address;
    private String email;
    private String phone;
    private String username;
    private String password;
    private Boolean enabled;
    private String role;
    private String status;

    public CustomerDTOImpl() {
    }

    public CustomerDTOImpl(Long customerId, String name, String address, String email, String phone,
                           String username, String password, Boolean enabled, String role) {
        this.customerId = customerId;
        this.name = name;
        this.address = address;
        this.email = email;
        this.phone = phone;
        this.username = username;
        this.password = "**********";
        this.enabled = enabled;
        this.role = role;
    }

    public CustomerDTOImpl(com.docker.atsea.model.Customer c) {
        this.customerId = c.getCustomerId();
        this.name = c.getName();
        this.address = c.getAddress();
        this.email = c.getEmail();
        this.phone = c.getPhone();
        this.username = c.getUsername();
        this.password = "**********";
        this.enabled = c.getEnabled();
        this.role = c.getRole();
    }

    public Long getCustomerId() {
        return customerId;
    }

    public void setCustomerId(Long customerId) {
        this.customerId = customerId;
    }

    ;

    public void setName(String name) {
        this.name = name;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public void setEnabled(Boolean enabled) {
        this.enabled = enabled;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getAddress() {
        return address;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public Boolean getEnabled() {
        return enabled;
    }

    public String getRole() {
        return role;
    }


    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof CustomerDTO)) return false;
        CustomerDTO that = (CustomerDTO) o;
        return Objects.equals(getCustomerId(), that.getCustomerId()) && Objects.equals(getName(), that.getName()) && Objects.equals(getAddress(), that.getAddress()) && Objects.equals(getEmail(), that.getEmail()) && Objects.equals(getPhone(), that.getPhone()) && Objects.equals(getUsername(), that.getUsername()) && Objects.equals(getPassword(), that.getPassword()) && Objects.equals(getEnabled(), that.getEnabled()) && Objects.equals(getRole(), that.getRole());
    }

    @Override
    public int hashCode() {
        return Objects.hash(getCustomerId(), getName(), getAddress(), getEmail(), getPhone(), getUsername(), getPassword(), getEnabled(), getRole());
    }

    @Override
    public String toString() {
        return "CustomerDTO{" +
                "customerId=" + customerId +
                ", name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", email='" + email + '\'' +
                ", phone='" + phone + '\'' +
                ", username='" + username + '\'' +
                ", password='" + password + '\'' +
                ", enabled=" + enabled +
                ", role='" + role + '\'' +
                '}';
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
