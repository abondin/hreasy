package ru.abondin.hreasy.apifacade;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = ThymeleafAutoConfiguration.class)
public class HreasyApiFacadeApplication {

    public static void main(String[] args) {
        SpringApplication.run(HreasyApiFacadeApplication.class, args);
    }

}
