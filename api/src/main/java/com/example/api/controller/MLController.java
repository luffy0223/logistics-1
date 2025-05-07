package com.example.api.controller;

import com.example.api.utils.MachineLearning;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/ml")
public class MLController {
    @Autowired
    private MachineLearning machineLearning;

    // 逻辑回归预测 urgent（二分类）
    @GetMapping("/urgent")
    public List<String> predictUrgent() {
        return machineLearning.runLogisticRegression();
    }

    // 线性回归预测 status（回归）
    @GetMapping("/linear")
    public List<String> predictStatusLinear() {
        return machineLearning.runLinearRegression();
    }

    // 决策树分类预测 status（分类）
    @GetMapping("/decision-tree")
    public List<String> predictStatusDecisionTree() {
        return machineLearning.runDecisionTree();
    }

    // KMeans聚类
    @GetMapping("/kmeans")
    public List<String> clusterData() {
        return machineLearning.runKMeans();
    }
}
