package com.example.servingwebcontent;

import com.example.service.StorageProperties;
import com.example.service.StorageService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.data.jpa.repository.config.EnableJpaRepositories;
import org.springframework.boot.autoconfigure.domain.EntityScan;

@SpringBootApplication
@EntityScan("com.example.entity")
@ComponentScan("com.example")
@EnableJpaRepositories("com.example.repository")
@EnableConfigurationProperties(StorageProperties.class)
public class CodeAnalysisApplication {

    public static void main(String[] args) {
        SpringApplication.run(CodeAnalysisApplication.class, args);
    }

    @Bean
    CommandLineRunner init(StorageService storageService) {
        return (args) -> {
            storageService.deleteAll();
            storageService.init();
        };
    }

}
