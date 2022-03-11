package ru.abondin.hreasy.platform.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.abondin.hreasy.platform.TestDataContainer;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.config.HrEasySecurityProps;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.TestFixedDataTimeConfig;

import java.time.Duration;
import java.time.OffsetDateTime;

@ActiveProfiles({"test", "dev"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Import(TestFixedDataTimeConfig.class)
@Slf4j
public class UpcomingVacationNotificationServiceTest {

    private final static Duration MONO_DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    @Autowired
    private UpcomingVacationNotificationService sender;

    @Autowired
    private AuthHandler authHandler;

    @Autowired
    private TestDataContainer testData;

    @Autowired
    private HrEasySecurityProps securityProps;

    @Autowired
    private DateTimeService dateTimeService;

    @BeforeEach
    protected void validateTestConfiguration() {
        testData.initAsync().block(MONO_DEFAULT_TIMEOUT);
        var now = OffsetDateTime.now();
        ((TestFixedDataTimeConfig.TestFixedDataTimeService) dateTimeService).init(now);
    }

    @Test
    @DisplayName("Upcoming Vacation Notifications")
    public void testUpcomingVacationNotification() {
        dateTimeService.now();
    }

    private void generateVacations() {

    }


}
