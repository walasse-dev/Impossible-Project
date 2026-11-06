package com.marketplace.entities;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "product")
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private String name;
    private String description;
    @Column(precision = 12, scale=2, nullable=false)
    private BigDecimal price;
    private int stockQuantity;

    public Product(String name, String description, BigDecimal price, int stockQuantity) {
        if (name == null || name.isBlank() || stockQuantity < 0 || price == null || price.compareTo(BigDecimal.ZERO) <= 0) {
            throw  new IllegalArgumentException("Error in Product instantiation.");
        }

        this.name = name;
        this.description = description;
        this.price = price;
        this.stockQuantity = stockQuantity;
    }
}


