package com.example.api.model.entity;

import lombok.Data;

import java.util.List;

@Data
public class DistributionBarVO {

    private String address;  // 地址
    private List<CareCount> careCounts;  // 每个care的数量

}
