package com.petroleum.models;

import lombok.Data;

@Data
public class GenerateForm {
    private Integer amount;
    private Integer minNumber;
    private Integer maxNumber;
    private Boolean download;
}
