package com.example.api.test;

import com.example.api.model.entity.Distribution;
import com.example.api.model.entity.Driver;
import com.example.api.repository.DistributionRepository;
import com.example.api.repository.DriverRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@SpringBootTest
public class DistributionServiceTest {


    @Resource
    private DistributionRepository distributionRepository;

    @Test
    public void testInsertCsvData() throws Exception {
        InputStream is = getClass().getClassLoader().getResourceAsStream("transport_records.csv");
        assert is != null;
        BufferedReader reader = new BufferedReader(new InputStreamReader(is));

        boolean isFirstLine = true;

        List<String> lines = new ArrayList<>();

        String line;
        while ((line = reader.readLine()) != null) {
            if (isFirstLine) { // 跳过表头
                isFirstLine = false;
                continue;
            }

            String l = new String(line);
            lines.add(l);

        }

        for (String s : lines) {
            String[] fields = s.split(",");

            SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

            Distribution d = new Distribution();
            d.setId(fields[0]);
            d.setDid(fields[1]);
            d.setVid(fields[2]);
            d.setAddress(fields[3]);
            d.setCare(fields[4]);
            d.setDriver(fields[5]);
            d.setNumber(fields[6]);
            d.setPhone(fields[7]);
            d.setStatus(Integer.parseInt(fields[8]));
            d.setTime(fields[9]);
            d.setUrgent(Integer.parseInt(fields[10]) == 1);

            distributionRepository.save(d);
        }

        reader.close();

        // 验证插入是否成功（例如断言插入的数量）
        List<Distribution> all = distributionRepository.findAll();
        Assertions.assertFalse(all.isEmpty());
    }

}
