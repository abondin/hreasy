package ru.abondin.hreasy.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "hreasy.web.sec")
@Data
public class HrEasySecurityProps {
    /**
     * Allow to login employee using internal password stored in local DB
     */
    private Boolean internalPasswordEnabled = true;
    /**
     * Web session max inactive interval
     */
    private Duration maxInactiveInterval = Duration.ofDays(7);
    /**
     * Cookie max age
     */
    private Duration cookieMaxAge = Duration.ofHours(48);

    /**
     * Allowed origin for cors
     */
    private List<String> corsAllowedOrigins = new ArrayList<>();

    /**
     * Security User maps to database employee entity using email.
     * So, we have to map security username to employee email
     */
    private String emailSuffix = "@stm-labs.ru";


    /**
     * For development purposes only.
     * Auth every user with the secret master password
     */
    private String masterPassword = "";

    private String telegramJwtSecret = "hreasy-secret-for-jwt-tokens-for-internal-microsevices-comminications";

}
