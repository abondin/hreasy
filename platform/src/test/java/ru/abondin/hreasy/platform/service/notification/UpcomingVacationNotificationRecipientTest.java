package ru.abondin.hreasy.platform.service.notification;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
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
import ru.abondin.hreasy.platform.TestDataContainer;
import ru.abondin.hreasy.platform.config.BackgroundTasksProps;
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
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.argThat;
import static ru.abondin.hreasy.platform.TestDataContainer.emailFromUserName;
import static ru.abondin.hreasy.platform.TestEmployees.*;

/**
 * Verify that information about upcoming vacation sends to
 * <ol>
 * <li>Employee</li>
 * <li>All not fired managers of employee's project</li>
 * <li>All not fired managers of project's business account</li>
 * <li>All not fired  managers of project's department</li>
 * </ol>
 */
@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Import(TestFixedDataTimeConfig.class)
@Slf4j
public class UpcomingVacationNotificationRecipientTest extends BaseServiceTest {

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

    @Autowired
    private BackgroundTasksProps backgroundTasksProps;


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
    @DisplayName("Notifications Recipients Test for employee Haiden Spooner")
    public void testVacationEmployeeHaiden() {

        var vacationHaiden = defaultNewVacationBody();
        vacationHaiden.setStartDate(dateTimeService.now().plus(5, ChronoUnit.DAYS).toLocalDate());
        vacationHaiden.setEndDate(dateTimeService.now().plus(10, ChronoUnit.DAYS).toLocalDate());
        vacationHaiden.setStatus(VacationDto.VacationStatus.PLANNED);
        // 1. Save new planned vacation
        vacationService.create(auth, testData.employees.get(Billing_Empl_Haiden_Spooner), vacationHaiden)
                // 2. Start job to notify all incoming vacations
                .thenMany(sender.notifyUpcomingVacations())
                .blockLast(MONO_DEFAULT_TIMEOUT);

        // 3. Verify recipients in sent email
        Mockito.verify(emailMessageSender)
                .sendMessage(argThat((HrEasyEmailMessage mail) -> {
                    log.info("Employee haiden email {}", mail);
                    // Only employee himself should be in 'to' address
                    Assertions.assertEquals(1, mail.getTo().size(), "Only one recipient expected in 'to'");
                    Assertions.assertEquals(emailFromUserName(Billing_Empl_Haiden_Spooner), mail.getTo().get(0).toLowerCase(Locale.ROOT),
                            "Unexpected employee in 'to' recipients");


                    // All employees NOT FIRED managers should be in 'cc' address
                    // Also all emails from 'hreasy.background.upcoming-vacation.additional-email-addresses' should be added to 'cc'
                    // Haiden Spooner is empoyee of Billing project with 2 not fired managers, 1 manager of BA, 1 manager of department
                    // + abondin@gmail.com in 'hreasy.background.upcoming-vacation.additional-email-addresses'
                    Assertions.assertEquals(5, mail.getCc().size(), "unexpected employee recipients in 'cc'");
                    Assertions.assertTrue(mail.getCc().stream().map(String::toLowerCase).collect(Collectors.toList()).containsAll(Stream
                            .of(Multiprojet_Manager_Kyran_Neville, Billing_Manager_Maxwell_May, Billing_BA_Head_Husnain_Patterson, DevHead_Percy_Gough)
                            .map(TestDataContainer::emailFromUserName)
                            .toList()), "Unexpected employees in 'cc' recipients");
                    Assertions.assertTrue(mail.getCc().stream().map(String::toLowerCase).collect(Collectors.toList()).containsAll(
                            backgroundTasksProps.getUpcomingVacation().getAdditionalEmailAddresses()
                    ), "Unexpected employees in 'cc' recipients");
                    return true;
                }));

    }

    @Test
    @DisplayName("Notifications Recipients Test for ba head Husnain Patterson")
    public void testVacationBAHeadHusnain() {
        var vacationHusnain = defaultNewVacationBody();
        vacationHusnain.setStartDate(dateTimeService.now().plus(5, ChronoUnit.DAYS).toLocalDate());
        vacationHusnain.setEndDate(dateTimeService.now().plus(10, ChronoUnit.DAYS).toLocalDate());
        vacationHusnain.setStatus(VacationDto.VacationStatus.PLANNED);
        // 1. Save new planned vacation
        vacationService.create(auth, testData.employees.get(Billing_BA_Head_Husnain_Patterson), vacationHusnain)
                // 2. Start job to notify all incoming vacations
                .thenMany(sender.notifyUpcomingVacations())
                .blockLast(MONO_DEFAULT_TIMEOUT);

        // 3. Verify recipients in sent email
        Mockito.verify(emailMessageSender)
                .sendMessage(argThat((HrEasyEmailMessage mail) -> {
                    log.info("Business account head Husnain's email {}", mail);
                    // Only employee himself should be in 'to' address
                    Assertions.assertEquals(1, mail.getTo().size(), "Only one recipient expected in 'to'");
                    Assertions.assertEquals(emailFromUserName(Billing_BA_Head_Husnain_Patterson), mail.getTo().get(0).toLowerCase(Locale.ROOT),
                            "Unexpected employee in 'to' recipients");

                    // Husnain is manager for himself. He is employee of Billing project and head of Billing business account at the same time.
                    // Let's check that emails in 'to' are not duplicated in 'cc'.
                    Assertions.assertEquals(4, mail.getCc().size(), "unexpected employee recipients in 'cc'");
                    Assertions.assertTrue(mail.getCc().stream().map(String::toLowerCase).collect(Collectors.toList()).containsAll(Stream
                            .of(Multiprojet_Manager_Kyran_Neville, Billing_Manager_Maxwell_May, DevHead_Percy_Gough)
                            .map(TestDataContainer::emailFromUserName)
                            .toList()), "Unexpected employees in 'cc' recipients");
                    Assertions.assertTrue(mail.getCc().stream().map(String::toLowerCase).collect(Collectors.toList()).containsAll(
                            backgroundTasksProps.getUpcomingVacation().getAdditionalEmailAddresses()
                    ), "Unexpected employees in 'cc' recipients");
                    return true;
                }));
    }


    @Test
    @DisplayName("Notifications Recipients Test for dev head Percy Gough")
    public void testVacationDevHeadPercyGough() {
        var vacationPercy = defaultNewVacationBody();
        vacationPercy.setStartDate(dateTimeService.now().plus(5, ChronoUnit.DAYS).toLocalDate());
        vacationPercy.setEndDate(dateTimeService.now().plus(10, ChronoUnit.DAYS).toLocalDate());
        vacationPercy.setStatus(VacationDto.VacationStatus.PLANNED);
        // 1. Save new planned vacation
        vacationService.create(auth, testData.employees.get(DevHead_Percy_Gough), vacationPercy)
                // 2. Start job to notify all incoming vacations
                .thenMany(sender.notifyUpcomingVacations())
                .blockLast(MONO_DEFAULT_TIMEOUT);

        // 3. Verify recipients in sent email
        Mockito.verify(emailMessageSender)
                .sendMessage(argThat((HrEasyEmailMessage mail) -> {
                    log.info("Department head Percy's email {}", mail);
                    // Only employee himself should be in 'to' address
                    Assertions.assertEquals(1, mail.getTo().size(), "Only one recipient expected in 'to'");
                    Assertions.assertEquals(emailFromUserName(DevHead_Percy_Gough), mail.getTo().get(0).toLowerCase(Locale.ROOT),
                            "Unexpected employee in 'to' recipients");

                    // Percy is department head. No other managers over him
                    Assertions.assertEquals(1, mail.getCc().size(), "unexpected employee recipients in 'cc'");
                    Assertions.assertTrue(mail.getCc().stream().map(String::toLowerCase).collect(Collectors.toList()).containsAll(
                            backgroundTasksProps.getUpcomingVacation().getAdditionalEmailAddresses()
                    ), "Unexpected employees in 'cc' recipients");
                    return true;
                }));
    }

    @Test
    @DisplayName("Notifications Recipients Test for Jonas Martin - employee on project with no managers")
    public void testVacationEmployeeOnProjectWithoutManagers() {
        var vacationJonas = defaultNewVacationBody();
        vacationJonas.setStartDate(dateTimeService.now().plus(5, ChronoUnit.DAYS).toLocalDate());
        vacationJonas.setEndDate(dateTimeService.now().plus(10, ChronoUnit.DAYS).toLocalDate());
        vacationJonas.setStatus(VacationDto.VacationStatus.PLANNED);
        // 1. Save new planned vacation
        vacationService.create(auth, testData.employees.get(Integrator_Jonas_Martin), vacationJonas)
                // 2. Start job to notify all incoming vacations
                .thenMany(sender.notifyUpcomingVacations())
                .blockLast(MONO_DEFAULT_TIMEOUT);

        // 3. Verify recipients in sent email
        Mockito.verify(emailMessageSender)
                .sendMessage(argThat((HrEasyEmailMessage mail) -> {
                    log.info("Department head Percy's email {}", mail);
                    // Only employee himself should be in 'to' address
                    Assertions.assertEquals(1, mail.getTo().size(), "Only one recipient expected in 'to'");
                    Assertions.assertEquals(emailFromUserName(Integrator_Jonas_Martin), mail.getTo().get(0).toLowerCase(Locale.ROOT),
                            "Unexpected employee in 'to' recipients");

                    // no managers for M1 ERP Integration project or integration department. Project has no ba assigned
                    Assertions.assertEquals(1, mail.getCc().size(), "unexpected employee recipients in 'cc'");
                    Assertions.assertTrue(mail.getCc().stream().map(String::toLowerCase).collect(Collectors.toList()).containsAll(
                            backgroundTasksProps.getUpcomingVacation().getAdditionalEmailAddresses()
                    ), "Unexpected employees in 'cc' recipients");
                    return true;
                }));
    }

    private VacationCreateOrUpdateDto defaultNewVacationBody() {
        var vacation = new VacationCreateOrUpdateDto();
        vacation.setYear(dateTimeService.now().getYear());
        vacation.setDaysNumber(14);
        return vacation;
    }


}
