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
public class Transfer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(nullable = false)
    private Long id;
    @Column(nullable = false)
    private String client;
    @ManyToOne(cascade={CascadeType.DETACH}, optional = false)
    private Product product;
    @Column(nullable = false)
    private double volume;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(columnDefinition = "DATE")
    private LocalDate loadingDate;
    @ManyToOne(cascade={CascadeType.DETACH}, optional = false)
    private Depot loadingDepot;
    private String transporter;
    private String truckNumber;
    private String driver;
    @ManyToOne(cascade={CascadeType.DETACH}, optional = false)
    private Depot deliveryPlace;
    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
    @Column(columnDefinition = "DATE")
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
        if(this.client != null) this.client = this.client.trim().toUpperCase();
        if(this.transporter != null) this.transporter = this.transporter.trim().toUpperCase();
        if(this.truckNumber != null) this.truckNumber = this.truckNumber.trim().toUpperCase();
        if(this.driver != null) this.driver = this.driver.trim().toUpperCase();
    }
}
