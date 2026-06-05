package ru.abondin.hreasy.notifyms.config;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.validation.annotation.Validated;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.EnumSet;
import java.util.Set;

@Data
@Validated
@ConfigurationProperties("hreasy.notifications")
public class NotificationProperties {
    private String httpToken;
    private Worker worker = new Worker();
    private Retention retention = new Retention();
    private WorkingHours workingHours = new WorkingHours();
    private Channels channels = new Channels();

    @Data
    public static class Worker {
        private boolean enabled = true;
        @DurationUnit(ChronoUnit.SECONDS)
        private Duration fixedDelay = Duration.ofSeconds(10);
        @Min(1)
        private int batchSize = 20;
    }

    @Data
    public static class Retention {
        private boolean enabled = true;
        @DurationUnit(ChronoUnit.DAYS)
        private Duration maxAge = Duration.ofDays(365);
        private String cron = "0 45 3 * * *";
    }

    @Data
    public static class WorkingHours {
        @NotBlank
        private String timezone = "Europe/Moscow";
        private LocalTime start = LocalTime.of(9, 0);
        private LocalTime end = LocalTime.of(18, 0);
        private Set<DayOfWeek> workdays = EnumSet.of(
                DayOfWeek.MONDAY,
                DayOfWeek.TUESDAY,
                DayOfWeek.WEDNESDAY,
                DayOfWeek.THURSDAY,
                DayOfWeek.FRIDAY
        );
    }

    @Data
    public static class Channels {
        private YandexMessenger yandexMessenger = new YandexMessenger();
        private Email email = new Email();
    }

    @Data
    public static class YandexMessenger {
        private boolean enabled;
        private String mode = "business-hours-immediate";
        @Min(1)
        private int maxAttempts = 5;
        private String baseUrl = "https://botapi.messenger.yandex.net/bot/v1";
        private String oauthToken;
    }

    @Data
    public static class Email {
        private boolean enabled;
        private String mode = "daily-digest";
        @Min(1)
        private int maxAttempts = 3;
        private LocalTime digestTime = LocalTime.of(18, 30);
    }
}
