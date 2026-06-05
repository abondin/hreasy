package ru.abondin.hreasy.platform.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;

import java.time.OffsetDateTime;

// Configuration should be imported manually in test
// @Configuration
public class TestFixedDataTimeConfig {

    @Primary
    @Bean
    public DateTimeService dateTimeService() {
        return new TestFixedDataTimeService();
    }


    public static class TestFixedDataTimeService extends DateTimeService {
        private OffsetDateTime time;

        @Override
        public OffsetDateTime now() {
            if (time == null) {
                return super.now();
            }
            return time;
        }

        public void init(OffsetDateTime now) {
            this.time = now;
        }
    }
}
