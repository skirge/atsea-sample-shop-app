package com.docker.atsea.model;

import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import javax.persistence.*;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.hibernate.annotations.CreationTimestamp;

@Entity
@Table(name = "purchases")
public class Purchase {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long purchaseId;

    @CreationTimestamp
    @Temporal(TemporalType.TIMESTAMP)
    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Date purchasedAt;

    @JsonProperty(access = JsonProperty.Access.READ_ONLY)
    private Long customerId;

    @Column(length = 4096)
    private String firstName;

    @Column(length = 4096)
    private String lastName;

    @Column(length = 4096)
    private String company;

    @Column(length = 4096)
    private String title;

    @Column(length = 4096)
    private String address;

    @Column(length = 4096)
    private String city;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "purchasequantities", joinColumns = @JoinColumn(name = "purchaseid"))
    @MapKeyColumn(name = "productid")
    @Column(name = "quantity")
    private Map<Integer, Integer> productsOrdered = new HashMap<>();

    public Long getPurchaseId() { return purchaseId; }
    public Date getPurchasedAt() { return purchasedAt; }
    public Long getCustomerId() { return customerId; }
    public void setCustomerId(Long customerId) { this.customerId = customerId; }
    public Map<Integer, Integer> getProductsOrdered() { return productsOrdered; }
    public void setProductsOrdered(Map<Integer, Integer> productsOrdered) {
        this.productsOrdered = productsOrdered;
    }

    public String getFirstName() { return firstName; }
    public void setFirstName(String firstName) { this.firstName = firstName; }
    public String getLastName() { return lastName; }
    public void setLastName(String lastName) { this.lastName = lastName; }
    public String getCompany() { return company; }
    public void setCompany(String company) { this.company = company; }
    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }
    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }
    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }
}
