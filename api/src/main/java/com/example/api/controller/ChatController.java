package com.example.api.controller;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import com.example.api.model.entity.*;
import com.example.api.repository.DistributionRepository;
import com.example.api.utils.MachineLearning;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 */
@RestController
    @RequestMapping("/api/eChat")
public class ChatController {

    @Resource
    private DistributionRepository distributionRepository;

    @Resource
    private MachineLearning machineLearning;

    @GetMapping("/distribution")
    public List<DistributionChatVO> getDistribution(String address) {
// 根据传入的address查询所有相关的Distribution记录
        List<Distribution> distributions = distributionRepository.findByAddress(address);

        // 使用一个Map来记录care的数量
        Map<String, Integer> careCountMap = new HashMap<>();

        // 遍历Distribution列表，统计每个care的数量
        for (Distribution distribution : distributions) {
            String care = distribution.getCare();  // 获取care字段的值
            careCountMap.put(care, careCountMap.getOrDefault(care, 0) + 1);
        }

        // 将care和计数转换为DistributionChatVO对象
        List<DistributionChatVO> result = new ArrayList<>();
        for (Map.Entry<String, Integer> entry : careCountMap.entrySet()) {
            DistributionChatVO vo = new DistributionChatVO();
            vo.setName(entry.getKey());  // 设置care名称
            vo.setValue(entry.getValue());  // 设置care的数量
            result.add(vo);
        }

        // 返回结果
        return result;
    }

    @GetMapping("/distribution-bar")
    public List<DistributionBarVO> getDistributionBar() {
        // 查询所有的Distribution记录
        List<Distribution> distributions = distributionRepository.findAll();

        // 使用一个Map来记录每个地址的care计数
        Map<String, Map<String, Integer>> addressCareCountMap = new HashMap<>();

        // 遍历Distribution列表，统计每个地址下的care数量
        for (Distribution distribution : distributions) {
            String address = distribution.getAddress();  // 获取地址
            String care = distribution.getCare();  // 获取care字段的值

            // 确保Map中有该地址
            addressCareCountMap.putIfAbsent(address, new HashMap<>());
            // 更新care计数
            Map<String, Integer> careCountMap = addressCareCountMap.get(address);
            careCountMap.put(care, careCountMap.getOrDefault(care, 0) + 1);
        }

        // 构造返回结果
        List<DistributionBarVO> result = new ArrayList<>();
        for (Map.Entry<String, Map<String, Integer>> entry : addressCareCountMap.entrySet()) {
            String address = entry.getKey();
            Map<String, Integer> careCountMap = entry.getValue();

            // 只选择前三个care
            List<CareCount> careCounts = careCountMap.entrySet().stream()
                    .map(e -> new CareCount(e.getKey(), e.getValue()))
                    .sorted(Comparator.comparingInt(CareCount::getCount).reversed())
                    .limit(3)
                    .collect(Collectors.toList());

            // 创建一个VO对象，并设置地址和前三个care的数量
            DistributionBarVO vo = new DistributionBarVO();
            vo.setAddress(address);
            vo.setCareCounts(careCounts);
            result.add(vo);
        }

        // 返回结果
        return result;
    }

    @GetMapping("/mlPredictionRate")
    public List<PredictionRateVO> getMlPredictionRate() {
        List<String> strings = machineLearning.runDecisionTree();
        List<PredictionRateVO> result = new ArrayList<>();

        // Temporary map to store address-based statistics
        Map<String, PredictionRateVO> addressMap = new HashMap<>();

        for (String string : strings) {
            JSONObject jsonObject = JSON.parseObject(string);
            String address = jsonObject.getString("address");
            double prediction = jsonObject.getDoubleValue("prediction");
            double care = jsonObject.getDoubleValue("care");

            // Get or create the PredictionRateVO for this address
            PredictionRateVO vo = addressMap.computeIfAbsent(address, addr -> {
                PredictionRateVO newVo = new PredictionRateVO();
                newVo.setAddress(addr);
                newVo.setCount(0);
                newVo.setSuccessCount(0);
                return newVo;
            });

            // Update counts
            vo.setCount(vo.getCount() + 1);
            if (prediction == care) {
                vo.setSuccessCount(vo.getSuccessCount() + 1);
            }
        }

        // Convert map values to list
        result.addAll(addressMap.values());
        return result;
    }


}
