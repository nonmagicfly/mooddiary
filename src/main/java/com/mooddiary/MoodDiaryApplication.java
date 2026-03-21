package com.mooddiary;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan
public class MoodDiaryApplication {
    public static void main(String[] args) {
        SpringApplication.run(MoodDiaryApplication.class, args);
    }
}

