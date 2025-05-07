package com.example.api.repository;

import com.example.api.model.entity.Distribution;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface DistributionRepository extends JpaRepository<Distribution, String> {

    // 使用@Query注解来编写自定义查询
    @Query("SELECT d FROM Distribution d WHERE d.address = :address")
    List<Distribution> findByAddress(@Param("address") String address);

}
