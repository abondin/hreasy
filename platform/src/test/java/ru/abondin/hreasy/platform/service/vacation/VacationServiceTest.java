package ru.abondin.hreasy.platform.service.vacation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.r2dbc.core.R2dbcEntityTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.repo.notification.UpcomingVacationNotificationLogRepo;
import ru.abondin.hreasy.platform.repo.vacation.VacPlanningPeriodEntry;
import ru.abondin.hreasy.platform.repo.vacation.VacPlanningPeriodRepo;
import ru.abondin.hreasy.platform.repo.vacation.VacationRepo;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.TestFixedDataTimeConfig;
import ru.abondin.hreasy.platform.service.vacation.dto.VacationRequestDto;

import java.time.OffsetDateTime;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Import(TestFixedDataTimeConfig.class)
class VacationServiceTest extends BaseServiceTest {

    @Autowired
    private VacationService vacationService;

    @Autowired
    private DateTimeService dateTimeService;

    @Autowired
    private VacPlanningPeriodRepo planningPeriodRepo;

    @Autowired
    private VacationRepo vacationRepo;

    @Autowired
    private UpcomingVacationNotificationLogRepo upcomingVacationNotificationLogRepo;

    @Autowired
    private R2dbcEntityTemplate db;

    @BeforeEach
    void beforeEach() {
        var now = OffsetDateTime.now();
        ((TestFixedDataTimeConfig.TestFixedDataTimeService) dateTimeService).init(now);
        initEmployeesDataAndLogin();
        planningPeriodRepo.deleteAll()
                .then(upcomingVacationNotificationLogRepo.deleteAll())
                .then(vacationRepo.deleteAll())
                .then(db.insert(new VacPlanningPeriodEntry(
                        now.getYear(),
                        now,
                        auth.getEmployeeInfo().getEmployeeId(),
                        null,
                        null,
                        null
                )))
                .block(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    @DisplayName("Employee cannot request vacation with start date in the past")
    void rejectVacationRequestWithPastStartDate() {
        var request = new VacationRequestDto();
        request.setYear(dateTimeService.now().getYear());
        request.setDaysNumber(5);
        request.setStartDate(dateTimeService.now().toLocalDate().minusDays(1));
        request.setEndDate(dateTimeService.now().toLocalDate().plusDays(4));

        StepVerifier.create(vacationService.requestVacation(auth, request))
                .expectErrorMatches(error -> error instanceof BusinessError
                        && ((BusinessError) error).getCode().equals("errors.vacation.request.validation.start_date_in_past"))
                .verify(MONO_DEFAULT_TIMEOUT);
    }
}
