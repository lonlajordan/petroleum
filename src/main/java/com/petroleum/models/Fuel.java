package com.petroleum.models;

import lombok.Getter;
import lombok.Setter;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
@Table(name = "abp_fuel")
public class Fuel {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private double amount = 0;
    @Column(nullable = false)
    private int number = 0;
    @Column(nullable = false, unique = true)
    private String code = "";
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime date = LocalDateTime.now();
    @Column(name = "actif", nullable = false)
    private boolean enabled = true;
}
