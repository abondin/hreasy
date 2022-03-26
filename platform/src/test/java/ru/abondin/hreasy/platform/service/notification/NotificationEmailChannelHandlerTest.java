package ru.abondin.hreasy.platform.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationEmailChannelHandler;

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
@ActiveProfiles({"test", "dev"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class NotificationEmailChannelHandlerTest {

    @Autowired
    private NotificationEmailChannelHandler emailChannelHandler;

    @Test
    public void testSimpleEmail(){
//        NotificationRoute route = NotificationRoute.fromSystem();
//        StepVerifier.create(emailChannelHandler.handleNotification()).expectNextCount(1).verifyComplete();
    }
}
