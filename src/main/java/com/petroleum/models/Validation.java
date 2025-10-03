package com.petroleum.models;

import lombok.Data;

@Data
public class Validation {
    private Long fuelId;
    private Long productId;
    private Long stationId;
    private String code;
    private String matriculation;
}
