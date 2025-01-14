package com.laptrinh.k8s;

import jakarta.persistence.*;

@Entity // Marks this class as a JPA entity
@Table(name = "products") // Specifies the table name in the database
public class Product {

    @Id // Marks this field as the primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY) // Auto-generates the ID
    private Long id;

    @Column(nullable = false) // Specifies a non-nullable column
    private String name;

    @Column(nullable = false) // Specifies a non-nullable column
    private double price;

    // Default constructor (required by JPA)
    public Product() {
    }

    // Parameterized constructor for easier object creation
    public Product(String name, double price) {
        this.name = name;
        this.price = price;
    }

    // Getters and setters
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
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

    @Override
    public String toString() {
        return "Product{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", price=" + price +
                '}';
    }
}
