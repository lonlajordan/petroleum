package com.petroleum.models;

import com.petroleum.enums.Status;
import com.petroleum.enums.Step;
import lombok.Getter;
import lombok.Setter;
import org.springframework.format.annotation.DateTimeFormat;

import javax.persistence.*;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Invoice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private String client;
    @ManyToOne
    private Product product;
    @Column(nullable = false)
    private int volume;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate loadingDate;
    private String loadingDepot;
    private String transporter;
    private String truckNumber;
    private String driver;
    private String deliveryPlace;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    private LocalDate receiptDate;
    @Column(columnDefinition = "TIMESTAMP")
    private LocalDateTime date = LocalDateTime.now();
    @Enumerated(EnumType.ORDINAL)
    private Status status = Status.PENDING;
    @Enumerated(EnumType.ORDINAL)
    private Step step = Step.OPERATING_OFFICER;
    private String reason = "";

    public boolean canDecide(boolean isDirector){
        return (isDirector && Step.DIRECTOR.equals(step)) || (!isDirector && Step.OPERATING_OFFICER.equals(step));
    }

    public void normalize(){

    }
}
