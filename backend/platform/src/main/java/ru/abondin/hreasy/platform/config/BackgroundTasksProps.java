package ru.abondin.hreasy.platform.config;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.convert.DurationUnit;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "hreasy.background")
@Data
public class BackgroundTasksProps {

    private UpcomingVacationProps upcomingVacation = new UpcomingVacationProps();
    private NotificationRetentionProps notificationRetention = new NotificationRetentionProps();
    private ProjectTransferRequestExpirationProps projectTransferRequestExpiration =
            new ProjectTransferRequestExpirationProps();

    /**
     * Buffer size to scan all vacations to notify
     * Technical property
     */
    private int defaultBufferSize = 1000;

    @Data
    public static class UpcomingVacationProps {
        /**
         * Notify employee if his vacation starts in 21 days or early
         */
        private int startTimeThresholdDays= 21;

        /**
         * Addresses in email copy
         */
        private List<String> additionalEmailAddresses = new ArrayList<>();

        private boolean jobEnabled = false;

    }

    @Data
    public static class NotificationRetentionProps {
        private boolean enabled = true;

        @DurationUnit(ChronoUnit.DAYS)
        private Duration maxAge = Duration.ofDays(365);

        private String cron = "0 30 3 * * *";
    }

    @Getter
    @Setter
    public static class ProjectTransferRequestExpirationProps {
        private boolean enabled = true;

        @DurationUnit(ChronoUnit.DAYS)
        private Duration maxAge = Duration.ofDays(14);

        private String cron = "0 0 2 * * *";
    }
}
