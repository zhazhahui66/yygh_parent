package com.xxxx.yygh.cmn;


import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;

/**
 * @author v
 */

@SpringBootApplication
@ComponentScan(basePackages = "com.xxxx")
@MapperScan("com.xxxx.yygh.cmn.mapper.xml")
@EnableDiscoveryClient
@EnableFeignClients("com.xxxx")
public class ServiceUserApplication {
    public static void main(String[] args) {
        SpringApplication.run(ServiceUserApplication.class,args);
    }
}
