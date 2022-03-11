package ru.abondin.hreasy.platform.service;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import java.time.OffsetDateTime;

@Configuration
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
                throw new RuntimeException("Time for Text Fixed Date Time Service must be initialized manually");
            }
            return super.now();
        }

        public void init(OffsetDateTime now) {
            this.time = now;
        }
    }
}
