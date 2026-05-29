package ru.abondin.hreasy.notifyms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class NotificationConfigValidator implements InitializingBean {
    private final NotificationProperties props;

    @Override
    public void afterPropertiesSet() {
        if (props.getChannels().getEmail().isEnabled()) {
            throw new IllegalStateException("Email digest channel is not implemented yet");
        }
    }
}
