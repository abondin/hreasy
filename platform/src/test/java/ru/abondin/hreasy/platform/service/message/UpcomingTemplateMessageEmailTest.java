package ru.abondin.hreasy.platform.service.message;

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
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.notification.upcomingvacations.UpcomingVacationNotificationTemplate;

import java.util.UUID;

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
public class UpcomingTemplateMessageEmailTest extends BaseServiceTest {

    @Autowired
    private EmailMessageSender sender;

    @Autowired
    private UpcomingVacationNotificationTemplate template;

    @Autowired
    private DateTimeService dateTimeService;

    @BeforeEach
    private void before() {
        initEmployeesDataAndLogin();
    }

    @Test
    public void testSimpleEmail() {
        var context = new UpcomingVacationNotificationTemplate.UpcomingVacationContext();
        context.setDaysNumber(14);
        context.setStartDate(dateTimeService.now().toLocalDate().plusDays(10));
        context.setEndDate(dateTimeService.now().toLocalDate().plusDays(24));
        context.setEmployeeEmail("Haiden.Spooner@stm-labs.ru");
        context.setEmployeeFirstname("Haiden");
        context.setClientUuid(UUID.randomUUID().toString());

        var message = template.create(context);

        StepVerifier.create(sender.sendMessage(message))
                .expectNextCount(1).verifyComplete();
    }
}
