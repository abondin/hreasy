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
import ru.abondin.hreasy.platform.service.notification.NotificationService;
import ru.abondin.hreasy.platform.service.notification.channels.NotificationChannelHandler;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;
import ru.abondin.hreasy.platform.service.notification.sender.NotificationSender;

import java.time.Duration;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static ru.abondin.hreasy.platform.TestEmployees.Admin_Shaan_Pitts;
import static ru.abondin.hreasy.platform.TestEmployees.FMS_Empl_Ammara_Knott;

@ActiveProfiles({"test", "dev"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class NotificationIntegrationTest {

    private final static Duration MONO_DEFAULT_TIMEOUT = Duration.ofSeconds(15);


    @Autowired
    private NotificationSender sender;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private AuthHandler authHandler;

    @Autowired
    private TestDataContainer testData;

    @Autowired
    private HrEasySecurityProps securityProps;

    private AuthContext auth;

    @BeforeEach
    protected void validateTestConfiguration() {
        if (securityProps.getMasterPassword().isBlank()) {
            Assertions.fail("No master password found");
        }
        this.auth = auth(Admin_Shaan_Pitts).block(MONO_DEFAULT_TIMEOUT);
        testData.initAsync().block(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    @DisplayName("Send one notification without any system template")
    public void testOneNotification() {
        var notification = new NewNotificationDto();
        var uuid = UUID.randomUUID().toString();
        notification.setCategory("test");
        notification.setClientUuid(uuid);
        notification.setDeliveryChannels(Arrays.asList(0));
        notification.setMarkdownText("**Test**");
        notification.setTitle("Test message");
        notification.setContext(new JSONObject(Map.of("testKey", "testValue")).toString());

        var route = NotificationChannelHandler.Route.fromSystem(
                Arrays.asList(testData.employees.get(Admin_Shaan_Pitts)));

        StepVerifier.create(
                sender.send(notification, route)
        ).expectNextCount(1).verifyComplete();

        StepVerifier.create(notificationService.myNotifications(this.auth)
                .filter(n->uuid.equals(n.getClientUuid()))
        ).expectNextMatches(n->{
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
        notification.setDeliveryChannels(Arrays.asList(0, 1));
        notification.setMarkdownText("**Test**");
        notification.setTitle("Test message");
        notification.setContext(new JSONObject(Map.of("testKey", "testValue")).toString());

        var route = NotificationChannelHandler.Route.fromSystem(
                Arrays.asList(testData.employees.get(Admin_Shaan_Pitts)));

        StepVerifier.create(
                sender.send(notification, route)
        ).expectNextCount(2).verifyComplete();

        StepVerifier.create(notificationService.myNotifications(this.auth)
                .filter(n->uuid.equals(n.getClientUuid()))
        ).expectNextMatches(n->{
            var match = true;
            match &= n.getClientUuid().equals(uuid);
            match &= n.getContext() != null;
            return match;
        }).verifyComplete();
    }


    private Mono<AuthContext> auth(String username) {
        return authHandler.login(new UsernamePasswordAuthenticationToken(
                TestDataContainer.emailFromUserName(username),
                securityProps.getMasterPassword())
        );
    }
}
