package ru.abondin.hreasy.telegram;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hreasy.telegram")
@Data
public class HrEasyBotProps {
    private String botUsername = "<BOT_NAME_IS_NOT_DEFINED>";
    private String botToken = "<BOT_TOKEN_IS_NOT_DEFINED>";
    private long botCreator = 1;
}
