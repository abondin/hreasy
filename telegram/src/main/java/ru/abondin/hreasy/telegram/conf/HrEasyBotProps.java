package ru.abondin.hreasy.telegram.conf;

import jakarta.validation.constraints.NotNull;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "hreasy.telegram")
@Data
public class HrEasyBotProps {
    private String botUsername = "BOT_NAME_IS_NOT_DEFINED";
    private String botToken = "BOT_TOKEN_IS_NOT_DEFINED";
    private long botCreator = 1;

    private Duration defaultBotActionTimeout = Duration.ofSeconds(30);
    private int defaultFetchSize = 5;

    private Platform platform = new Platform();

    private Duration defaultCacheTtl = Duration.ofMinutes(3);

    @Data
    public static class Platform {
        @NotNull
        private String jwtTokenSecret = "hreasy-secret-for-jwt-tokens-for-internal-microsevices-comminications";
        @NotNull
        private Duration jwtTokenExpiration = Duration.ofMinutes(5);

        private String baseUrl = "http://localhost:8081/telegram/";

        private String webInterfaceUrl = "https://hr.stm.local";

        // Must be less than defaultBotActionTimeout
        private Duration httpRequestTimeout = Duration.ofSeconds(10);
    }
}