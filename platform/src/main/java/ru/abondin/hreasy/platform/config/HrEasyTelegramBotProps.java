package ru.abondin.hreasy.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "hreasy.common.telegram")
@Data
public class HrEasyTelegramBotProps {
    private Duration linkedExpirationDuration = Duration.ofMinutes(10);
    private Duration linkGenerationInterval = Duration.ofMinutes(2);

    private int findEmployeeLevensteinThreshold = 3;
}
