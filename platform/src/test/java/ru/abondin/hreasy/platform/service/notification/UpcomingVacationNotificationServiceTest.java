package ru.abondin.hreasy.platform.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.SpyBean;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.TestPropertySource;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.repo.notification.UpcomingVacationNotificationLogRepo;
import ru.abondin.hreasy.platform.repo.vacation.VacationRepo;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.TestFixedDataTimeConfig;
import ru.abondin.hreasy.platform.service.message.DummyEmailMessageSender;
import ru.abondin.hreasy.platform.service.message.dto.HrEasyEmailMessage;
import ru.abondin.hreasy.platform.service.notification.upcomingvacations.UpcomingVacationNotificationService;
import ru.abondin.hreasy.platform.service.vacation.VacationService;
import ru.abondin.hreasy.platform.service.vacation.dto.VacationCreateOrUpdateDto;
import ru.abondin.hreasy.platform.service.vacation.dto.VacationDto;

import java.time.OffsetDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static ru.abondin.hreasy.platform.TestEmployees.Admin_Shaan_Pitts;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Import(TestFixedDataTimeConfig.class)
@Slf4j
public class UpcomingVacationNotificationServiceTest extends BaseServiceTest {

    @Autowired
    private UpcomingVacationNotificationService sender;

    @Autowired
    private VacationService vacationService;

    @Autowired
    private DateTimeService dateTimeService;

    @Autowired
    private UpcomingVacationNotificationLogRepo logRepo;

    @Autowired
    private VacationRepo vacationRepo;

    @SpyBean
    private DummyEmailMessageSender emailMessageSender;


    @BeforeEach
    protected void beforeEach() {
        var now = OffsetDateTime.now();
        ((TestFixedDataTimeConfig.TestFixedDataTimeService) dateTimeService).init(now);

        initEmployeesDataAndLogin();
        clearAllVacations();
    }

    private void clearAllVacations() {
        logRepo.deleteAll().then(vacationRepo.deleteAll()).block(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    @DisplayName("Upcoming Vacation Notifications")
    public void testUpcomingVacationNotification() {
        var createdVacationIds = generateVacations().collectList().block(MONO_DEFAULT_TIMEOUT);
        // Check that only last vacation notified
        log.info("-------- Check that only last vacation notified");
        StepVerifier.create(
                sender.notifyUpcomingVacations().thenMany(
                        logRepo.vacationsIn(createdVacationIds))
        ).expectNextCount(3).verifyComplete();
        Mockito.verify(emailMessageSender, times(3))
                .sendMessage(any(HrEasyEmailMessage.class));

        // Check that no vacations notified after first notifications
        Mockito.reset(emailMessageSender);
        log.info("-------- Check that no vacations notified after first notifications");
        sender.notifyUpcomingVacations().thenMany(logRepo.vacationsIn(createdVacationIds)).blockLast(MONO_DEFAULT_TIMEOUT);

        Mockito.verify(emailMessageSender, times(0))
                .sendMessage(any(HrEasyEmailMessage.class));
    }


    private Flux<Integer> generateVacations() {
        var createdVacationIds = new ArrayList<Mono<Integer>>();

        // 1. Planned vacation starts in 30 days (should not be notified)
        var vacation1 = defaultNewVacationBody();
        vacation1.setStartDate(dateTimeService.now().plus(30, ChronoUnit.DAYS).toLocalDate());
        vacation1.setEndDate(dateTimeService.now().plus(44, ChronoUnit.DAYS).toLocalDate());
        vacation1.setStatus(VacationDto.VacationStatus.PLANNED);
        createdVacationIds.add(vacationService.create(auth, testData.employees.get(Admin_Shaan_Pitts), vacation1));

        // 2. Planned vacation starts in 10 days. But status in CANCELED (should not be notified)
        var vacation2 = defaultNewVacationBody();
        vacation2.setStartDate(dateTimeService.now().plus(10, ChronoUnit.DAYS).toLocalDate());
        vacation2.setEndDate(dateTimeService.now().plus(24, ChronoUnit.DAYS).toLocalDate());
        vacation2.setStatus(VacationDto.VacationStatus.CANCELED);
        createdVacationIds.add(vacationService.create(auth, testData.employees.get(Admin_Shaan_Pitts), vacation2));

        // 3. Planned vacation starts in 10 days (should be notified)
        var vacation3 = defaultNewVacationBody();
        vacation3.setStartDate(dateTimeService.now().plus(10, ChronoUnit.DAYS).toLocalDate());
        vacation3.setEndDate(dateTimeService.now().plus(24, ChronoUnit.DAYS).toLocalDate());
        vacation3.setStatus(VacationDto.VacationStatus.PLANNED);
        createdVacationIds.add(vacationService.create(auth, testData.employees.get(Admin_Shaan_Pitts), vacation3));

        // 4. Vacation with compensation started tomorrow (should be notified)
        var vacation4 = defaultNewVacationBody();
        vacation4.setStartDate(dateTimeService.now().plus(1, ChronoUnit.DAYS).toLocalDate());
        vacation4.setEndDate(dateTimeService.now().plus(8, ChronoUnit.DAYS).toLocalDate());
        vacation4.setStatus(VacationDto.VacationStatus.COMPENSATION);
        createdVacationIds.add(vacationService.create(auth, testData.employees.get(Admin_Shaan_Pitts), vacation4));

        // 5. Planned vacation started today (should be notified)
        var vacation5 = defaultNewVacationBody();
        vacation5.setStartDate(dateTimeService.now().plus(0, ChronoUnit.DAYS).toLocalDate());
        vacation5.setEndDate(dateTimeService.now().plus(7, ChronoUnit.DAYS).toLocalDate());
        vacation5.setStatus(VacationDto.VacationStatus.PLANNED);
        createdVacationIds.add(vacationService.create(auth, testData.employees.get(Admin_Shaan_Pitts), vacation5));

        // 6. Planned vacation started yesterday (should not be notified)
        var vacation6 = defaultNewVacationBody();
        vacation6.setStartDate(dateTimeService.now().minus(1, ChronoUnit.DAYS).toLocalDate());
        vacation6.setEndDate(dateTimeService.now().plus(7, ChronoUnit.DAYS).toLocalDate());
        vacation6.setStatus(VacationDto.VacationStatus.PLANNED);
        createdVacationIds.add(vacationService.create(auth, testData.employees.get(Admin_Shaan_Pitts), vacation6));

        return Flux.concat(createdVacationIds);
    }

    private VacationCreateOrUpdateDto defaultNewVacationBody() {
        var vacation = new VacationCreateOrUpdateDto();
        vacation.setYear(dateTimeService.now().getYear());
        vacation.setDaysNumber(14);
        return vacation;
    }


}
