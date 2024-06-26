package ru.abondin.hreasy.platform;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.oauth2.server.servlet.OAuth2AuthorizationServerJwtAutoConfiguration;
import org.springframework.boot.autoconfigure.thymeleaf.ThymeleafAutoConfiguration;

@SpringBootApplication
@EnableAutoConfiguration(exclude = {
        ThymeleafAutoConfiguration.class,
        OAuth2AuthorizationServerJwtAutoConfiguration.class
})
public class HreasyPlatformApplication {

    public static void main(String[] args) {
        SpringApplication.run(HreasyPlatformApplication.class, args);
    }

}
