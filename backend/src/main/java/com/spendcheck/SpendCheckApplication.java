package com.spendcheck;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class SpendCheckApplication {

    public static void main(String[] args) {
        SpringApplication.run(SpendCheckApplication.class, args);
    }
}
