package ru.abondin.hreasy.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableAutoConfiguration(exclude = ThymeleafAutoConfiguration.class)
public class HreasyPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(HreasyPlatformApplication.class, args);
    }

}
