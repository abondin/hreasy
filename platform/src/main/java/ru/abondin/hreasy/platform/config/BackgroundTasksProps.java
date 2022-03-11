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


    @Data
    public static class UpcomingVacationProps {
        /**
         * Notify employee if his vacation starts in 21 days or early
         */
        private Duration startTimeThreshold = Duration.ofDays(21);

        /**
         * Addresses in email copy
         */
        private List<String> additionalEmailAddresses = new ArrayList<>();

        private boolean upcomingVacationJobEnabled = false;

    }
}
