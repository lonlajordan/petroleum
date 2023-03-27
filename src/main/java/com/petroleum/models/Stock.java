package com.petroleum.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(uniqueConstraints = {@UniqueConstraint(columnNames = {"depot_id", "product_id"}, name = "unique_depot_product")})
public class Stock {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @ManyToOne(cascade={CascadeType.DETACH}, optional = false)
    @JoinColumn(name = "depot_id")
    private Depot depot;
    @ManyToOne(cascade={CascadeType.DETACH}, optional = false)
    @JoinColumn(name = "product_id")
    private Product product;
    private double volume = 0;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime updatedAt = LocalDateTime.now();

    public Stock() {
    }

    public Stock(Depot depot, Product product) {
        this.depot = depot;
        this.product = product;
    }
}
