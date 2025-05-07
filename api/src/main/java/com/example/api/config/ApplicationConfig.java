package com.example.api.config;

import org.apache.spark.sql.SparkSession;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ApplicationConfig {


    @Bean
    public SparkSession sparkSession() {
        return SparkSession.builder()
                .appName("SpringBootSparkMLApp")
                .master("local[*]") // 本地模式
                .getOrCreate();
    }
}
