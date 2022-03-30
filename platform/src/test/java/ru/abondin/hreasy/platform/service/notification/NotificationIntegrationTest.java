package ru.abondin.hreasy.platform.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.TestDataContainer;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.config.HrEasySecurityProps;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationRoute;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;
import ru.abondin.hreasy.platform.service.notification.sender.NotificationSender;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static ru.abondin.hreasy.platform.TestEmployees.Admin_Shaan_Pitts;
import static ru.abondin.hreasy.platform.service.notification.sender.NotificationSender.NOTIFICATION_DELIVERY_CHANNEL_EMAIL;
import static ru.abondin.hreasy.platform.service.notification.sender.NotificationSender.NOTIFICATION_DELIVERY_CHANNEL_PERSIST;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class NotificationIntegrationTest extends BaseServiceTest {

    @Autowired
    private NotificationSender sender;

    @Autowired
    private NotificationService notificationService;

    @BeforeEach
    protected void validateTestConfiguration() {
        initEmployeesDataAndLogin();
    }

    @Test
    @DisplayName("Send one notification without any system template")
    public void testOneNotification() {
        var notification = new NewNotificationDto();
        var uuid = UUID.randomUUID().toString();
        notification.setCategory("test");
        notification.setClientUuid(uuid);
        notification.setDeliveryChannels(Arrays.asList(NOTIFICATION_DELIVERY_CHANNEL_PERSIST));
        notification.setMarkdownText("**Test**");
        notification.setTitle("Test message");
        notification.setContext(new JSONObject(Map.of("testKey", "testValue")).toString());

        var route = NotificationRoute.fromSystem(
                Arrays.asList(testData.employees.get(Admin_Shaan_Pitts)));

        StepVerifier.create(
                sender.send(notification, route)
        ).expectNextCount(1).verifyComplete();

        StepVerifier.create(notificationService.myNotifications(this.auth)
                .filter(n -> uuid.equals(n.getClientUuid()))
        ).expectNextMatches(n -> {
            var match = true;
            match &= n.getClientUuid().equals(uuid);
            match &= n.getContext() != null;
            return match;
        }).verifyComplete();
    }


    @Test
    @DisplayName("Send one notification with DB and email channel")
    public void testOneDBEmailNotification() {
        var notification = new NewNotificationDto();
        var uuid = UUID.randomUUID().toString();
        notification.setCategory("test");
        notification.setClientUuid(uuid);
        notification.setDeliveryChannels(Arrays.asList(NOTIFICATION_DELIVERY_CHANNEL_PERSIST, NOTIFICATION_DELIVERY_CHANNEL_EMAIL));
        notification.setMarkdownText("**Test**");
        notification.setTitle("Test message");
        notification.setContext(new JSONObject(Map.of("testKey", "testValue")).toString());

        var route = NotificationRoute.fromSystem(
                Arrays.asList(testData.employees.get(Admin_Shaan_Pitts)));

        StepVerifier.create(
                sender.send(notification, route)
        ).expectNextCount(2).verifyComplete();

        StepVerifier.create(notificationService.myNotifications(this.auth)
                .filter(n -> uuid.equals(n.getClientUuid()))
        ).expectNextMatches(n -> {
            var match = true;
            match &= n.getClientUuid().equals(uuid);
            match &= n.getContext() != null;
            return match;
        }).verifyComplete();
    }
}
