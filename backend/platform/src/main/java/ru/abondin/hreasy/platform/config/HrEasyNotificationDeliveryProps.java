package ru.abondin.hreasy.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "hreasy.notifications.delivery-service")
@Data
public class HrEasyNotificationDeliveryProps {
    private boolean enabled = false;
    private URI baseUrl = URI.create("http://127.0.0.1:8083");
    private String token;
    private Duration timeout = Duration.ofSeconds(3);
}
