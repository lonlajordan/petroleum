package com.petroleum.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    private double passage = 0;
    private double passageTax = 0;
    private double refinery = 0;
    private double specialTax = 0;
    private double transport = 0;
    private double marking = 0;
    private double markingTax = 0;
    @OneToMany(mappedBy="product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stock> stocks = new ArrayList<>();
    @OneToMany(mappedBy="product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invoice> invoices = new ArrayList<>();
    @OneToMany(mappedBy="product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Supply> supplies = new ArrayList<>();

    @Transient
    private double invoiceVolume = 0;
    @Transient
    private double transferVolume = 0;

    public Product() {
    }

    public Product(String name) {
        this.name = name;
    }

    public void normalize(){
        if(this.name != null) this.name = this.name.trim().toUpperCase();
    }
}
