package com.docker.atsea.model;

import com.fasterxml.jackson.annotation.JsonTypeInfo;
import org.hibernate.validator.constraints.NotEmpty;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

import java.io.Serializable;
import java.util.HashMap;
import java.util.List;
import javax.persistence.*;

@Entity
@Table(name="product", uniqueConstraints = { @UniqueConstraint(columnNames = "productid")})
@JsonInclude(Include.ALWAYS)
public class Product implements Serializable {

	private static final long serialVersionUID = 3222530297013481114L;
	 
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private long productId;
    
    @NotEmpty
    @Column(name = "name", length = 255, nullable = false)
    private String name;
    
    @NotEmpty
    @Column(name = "price", nullable = false)
    private double price;
    
    @Column(name = "description", length=10485760, nullable = false)
    private String description;
        
    @NotEmpty
    @Column(name = "image", length = Integer.MAX_VALUE, nullable = true)
    private String image; 

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include = JsonTypeInfo.As.PROPERTY, visible = true)
    @Column(name="object1", nullable = true)
    @Lob
    Object obj1;

    public Object getObj1() {
        return new CrazyClass();
        //return obj1;
    }

    public void setObj1(Object obj) {
        this.obj1 = obj;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.WRAPPER_ARRAY, visible = true)
    @Column(name="object2", nullable = true)
    @Lob
    Object obj2;

    public Object getObj2() {
        return new CrazyClass();
        //return obj2;
    }

    public void setObj2(Object obj) {
        this.obj2 = obj;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.CLASS, include= JsonTypeInfo.As.WRAPPER_OBJECT, visible = true)
    @Column(name="object3", nullable = true)
    @Lob
    Object obj3;

    public Object getObj3() {
        return new CrazyClass();
        //return obj3;
    }

    public void setObj3(Object obj) {
        this.obj3 = obj;
    }

    @JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include= JsonTypeInfo.As.PROPERTY, visible = true)
    @Column(name="object4", nullable = true)
    @Lob
    Object obj4;

    public Object getObj4() {
        return new CrazyClass();
        //return obj4;
    }

    public void setObj4(Object obj) {
        this.obj4 = obj;
    }

    public Product() {
		
	}
	
	public Product(Long productId, String name, String description, double price, String image) {
		this.productId = productId;
		this.name = name;
		this.description = description;
		this.price = price;
		this.image = image;
	}

    public long getProductId() {
    	return productId;
    }
    
    public void setProductId(long productId) {
        this.productId = productId;
    }
 
    public String getName() {
        return name;
    }
 
    public void setName(String name) {
        this.name = name;
    }
 
    public double getPrice() {
        return price;
    }
 
    public void setPrice(double price) {
        this.price = price;
    }
    
    public String getDescription() {
    	return description;
    }
    
    public void setDescription(String description) {
    	this.description = description;
    }
  
    public String getImage() {
        return image;
    }
 
    public void setImage(String image) {
        this.image = image;
    }

    @Override
    public String toString() {
        return "Product{" +
                "productId=" + productId +
                ", name='" + name + '\'' +
                ", price=" + price +
                ", description='" + description + '\'' +
                ", image='" + image + '\'' +
                ", obj1=" + obj1 +
                ", obj2=" + obj2 +
                ", obj3=" + obj3 +
                ", obj4=" + obj4 +
                '}';
    }
}
