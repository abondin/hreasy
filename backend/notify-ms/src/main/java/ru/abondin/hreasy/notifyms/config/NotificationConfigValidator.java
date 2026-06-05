package ru.abondin.hreasy.notifyms.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
@Slf4j
public class NotificationConfigValidator implements InitializingBean {
    private final NotificationProperties props;

    @Override
    public void afterPropertiesSet() {
        log.info("Notification service configuration workerEnabled={}, batchSize={}, yandexMessengerEnabled={}, emailEnabled={}, workingHoursTimezone={}",
                props.getWorker().isEnabled(),
                props.getWorker().getBatchSize(),
                props.getChannels().getYandexMessenger().isEnabled(),
                props.getChannels().getEmail().isEnabled(),
                props.getWorkingHours().getTimezone());
        if (props.getChannels().getEmail().isEnabled()) {
            throw new IllegalStateException("Email digest channel is not implemented yet");
        }
    }
}
