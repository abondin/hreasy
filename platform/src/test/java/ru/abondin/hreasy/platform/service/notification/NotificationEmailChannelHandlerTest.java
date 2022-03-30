package ru.abondin.hreasy.platform.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.notification.channels.email.NotificationEmailChannelHandler;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.util.Arrays;
import java.util.UUID;

import static ru.abondin.hreasy.platform.service.notification.sender.NotificationSender.NOTIFICATION_DELIVERY_CHANNEL_EMAIL;

/**
 * Only of manual run.
 * All mail server properties must be provided on run
 * <code>
 * spring.mail.host=smtp.yandex.ru
 * spring.mail.username=YYYY@yandex.ru
 * spring.mail.password=XXXX
 * spring.mail.port=465
 * spring.mail.protocol=smtps
 * </code>
 */
@Disabled
@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class NotificationEmailChannelHandlerTest extends BaseServiceTest {

    @Autowired
    private NotificationEmailChannelHandler emailChannelHandler;

    @BeforeEach
    private void before() {
        initEmployeesDataAndLogin();
    }

    @Test
    public void testSimpleEmail() {
        var notif = new NewNotificationDto();
        notif.setClientUuid(UUID.randomUUID().toString());
        notif.setCategory("upcoming-email");
        notif.setDeliveryChannels(Arrays.asList(NOTIFICATION_DELIVERY_CHANNEL_EMAIL));
        notif.setTitle("Test upcoming");
        notif.setMarkdownText("Test upcoming message");
        StepVerifier.create(emailChannelHandler.handleNotification(notif
                , Arrays.asList("abondin@gmail.com"))).expectNextCount(1).verifyComplete();
    }
}
