package com.gyf.partner_backend;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.gyf.partner_backend.mapper")
@EnableScheduling
public class PartnerBackendApplication {

    public static void main(String[] args) {
        SpringApplication.run(PartnerBackendApplication.class, args);
    }

}
