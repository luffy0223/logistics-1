package com.example.api.model.entity;

import lombok.Data;

@Data
// 用于表示柱状图中每个care的数量
public class CareCount {
    private String care;
    private Integer count;

    // 构造方法
    public CareCount(String care, Integer count) {
        this.care = care;
        this.count = count;
    }
}