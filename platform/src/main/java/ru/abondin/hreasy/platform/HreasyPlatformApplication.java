package ru.abondin.hreasy.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
public class HreasyPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(HreasyPlatformApplication.class, args);
    }

}
