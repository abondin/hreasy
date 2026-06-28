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
        var yandex = props.getChannels().getYandexMessenger();
        log.info("Notification service configuration workerEnabled={}, batchSize={}, yandexMessengerEnabled={}, yandexMessengerMode={}, yandexMessengerTokenConfigured={}, emailEnabled={}, workingHoursTimezone={}",
                props.getWorker().isEnabled(),
                props.getWorker().getBatchSize(),
                yandex.isEnabled(),
                yandex.getMode(),
                org.springframework.util.StringUtils.hasText(yandex.getOauthToken()),
                props.getChannels().getEmail().isEnabled(),
                props.getWorkingHours().getTimezone());
        if (props.getChannels().getEmail().isEnabled()) {
            throw new IllegalStateException("Email digest channel is not implemented yet");
        }
    }
}
