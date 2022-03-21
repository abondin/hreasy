package ru.abondin.hreasy.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;
import java.util.ArrayList;
import java.util.List;

@Configuration
@ConfigurationProperties(prefix = "hreasy.background")
@Data
public class BackgroundTasksProps {

    private UpcomingVacationProps upcomingVacation = new UpcomingVacationProps();

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

        private boolean upcomingVacationJobEnabled = false;

    }
}
