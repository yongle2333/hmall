package com.qiu.pay;


import com.qiu.api.config.DefaultFeignConfig;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;


@MapperScan("com.qiu.pay.mapper")
@EnableFeignClients(basePackages = "com.qiu.api.client",defaultConfiguration = DefaultFeignConfig.class)
@SpringBootApplication
public class PayApplication {
    public static void main(String[] args) {
        SpringApplication.run(PayApplication.class, args);
    }


}