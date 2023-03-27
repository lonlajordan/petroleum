package com.petroleum.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Supply {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private double volume = 0;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime date = LocalDateTime.now();
    @ManyToOne(cascade={CascadeType.DETACH}, optional = false)
    @JoinColumn(name="product_id")
    private Product product;
    @ManyToOne(cascade={CascadeType.DETACH}, optional = false)
    @JoinColumn(name="depot_id")
    private Depot depot;
}
