package ru.abondin.hreasy.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.thymeleaf.autoconfigure.ThymeleafAutoConfiguration;

@SpringBootApplication(exclude = ThymeleafAutoConfiguration.class)
public class HreasyPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(HreasyPlatformApplication.class, args);
    }

}
