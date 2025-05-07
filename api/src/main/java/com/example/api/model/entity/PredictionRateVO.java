package com.example.api.model.entity;

import lombok.Data;

@Data
public class PredictionRateVO {

    private String address;

    private Integer count;

    private Integer successCount;

}
