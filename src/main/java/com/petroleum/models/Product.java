package com.petroleum.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@Table(name = "abp_product", uniqueConstraints = {@UniqueConstraint(columnNames = {"name"}, name = "unique_name")})
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private String name;
    @Column(nullable = false)
    private double passage = 0;
    @Column(nullable = false)
    private double passageTax = 0;
    @Column(nullable = false)
    private double refinery = 0;
    @Column(nullable = false)
    private double specialTax = 0;
    @Column(nullable = false)
    private double transport = 0;
    @Column(nullable = false)
    private double marking = 0;
    @Column(nullable = false)
    private double markingTax = 0;
    @OneToMany(mappedBy="product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stock> stocks = new ArrayList<>();
    @OneToMany(mappedBy="product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Invoice> invoices = new ArrayList<>();
    @OneToMany(mappedBy="product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Supply> supplies = new ArrayList<>();
    @OneToMany(mappedBy="product", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Fuel> fuels = new ArrayList<>();

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
