package com.petroleum.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Depot {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false, unique = true)
    private String name;
    @OneToMany(mappedBy="depot", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Stock> stocks = new ArrayList<>();

    public Depot() {
    }

    public Depot(String name) {
        this.name = name;
    }

    public void normalize(){
        if(this.name != null) this.name = this.name.trim().toUpperCase();
    }
}
