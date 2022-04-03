package ru.abondin.hreasy.platform.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.json.JSONObject;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.util.Arrays;
import java.util.Map;
import java.util.UUID;

import static ru.abondin.hreasy.platform.TestEmployees.Admin_Shaan_Pitts;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class NotificationIntegrationTest extends BaseServiceTest {

    @Autowired
    private NotificationPersistService persistService;

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
        notification.setMarkdownText("**Test**");
        notification.setTitle("Test message");
        notification.setContext(new JSONObject(Map.of("testKey", "testValue")).toString());

        StepVerifier.create(
                persistService.sendNotificationTo(notification, Arrays.asList(testData.employees.get(Admin_Shaan_Pitts)), null)
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

}
