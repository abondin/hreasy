package ru.abondin.hreasy.platform.service.udr;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.repo.udr.JuniorEntry;
import ru.abondin.hreasy.platform.repo.udr.JuniorRepo;
import ru.abondin.hreasy.platform.repo.udr.JuniorReportEntry;
import ru.abondin.hreasy.platform.repo.udr.JuniorReportRepo;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.udr.dto.AddToJuniorRegistryBody;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
class JuniorRegistryServiceTest extends BaseServiceTest {

    @Autowired
    private JuniorRegistryService service;

    @Autowired
    private JuniorRepo juniorRepo;
    @Autowired
    private JuniorReportRepo reportRepo;
    @Autowired
    private DateTimeService dateTimeService;

    @Autowired
    private DatabaseClient db;

    @BeforeEach
    protected void beforeEach() {
        initEmployeesDataAndLogin();
        cleanTables().block(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    void testGetAll() {
        var junId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var mentorId = testData.employees.get(TestEmployees.FMS_Manager_Jawad_Mcghee);
        var reporterId = testData.employees.get(TestEmployees.FMS_Empl_Ammara_Knott);
        this.auth = auth(TestEmployees.Admin_Shaan_Pitts).block(MONO_DEFAULT_TIMEOUT);
        buildJunior(junId, mentorId).block(MONO_DEFAULT_TIMEOUT);

        this.auth = auth(TestEmployees.FMS_Empl_Ammara_Knott).block(MONO_DEFAULT_TIMEOUT);
        buildReport(junId).block(MONO_DEFAULT_TIMEOUT);

        this.auth = auth(TestEmployees.Admin_Shaan_Pitts).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(service.juniors(auth))
                .expectNextMatches(dto -> {
                    Assertions.assertEquals(junId, dto.getJunior().getId());
                    Assertions.assertEquals(mentorId, dto.getMentor().getId());
                    Assertions.assertEquals(1, dto.getReports().size());
                    Assertions.assertEquals(reporterId, dto.getReports().get(0).getCreatedBy().getId());
                    return true;
                })
                .verifyComplete();
    }

    @Test
    void testGetBa() {
        var junRndId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var junBillingId = testData.employees.get(TestEmployees.Billing_Empl_Asiyah_Bob);
        var mentorId = testData.employees.get(TestEmployees.FMS_Manager_Jawad_Mcghee);
        this.auth = auth(TestEmployees.Admin_Shaan_Pitts).block(MONO_DEFAULT_TIMEOUT);
        buildJunior(junRndId, mentorId).block(MONO_DEFAULT_TIMEOUT);
        var junBillingEntry = new JuniorEntry();
        junBillingEntry.setJuniorId(junBillingId);
        junBillingEntry.setMentorId(mentorId);
        junBillingEntry.setBudgetingAccount(testData.ba_Billing());
        junBillingEntry.setCreatedAt(dateTimeService.now());
        junBillingEntry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        junBillingEntry.setRole("Java Developer");
        juniorRepo.save(junBillingEntry).block(MONO_DEFAULT_TIMEOUT);

        this.auth = auth(TestEmployees.Billing_BA_Head_Husnain_Patterson).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(service.juniors(auth))
                .expectNextMatches(dto -> dto.getJunior().getId() == junBillingId)
                .verifyComplete();
    }

    @Test
    void testAddJunior() {
        this.auth = auth(TestEmployees.Admin_Shaan_Pitts).block(MONO_DEFAULT_TIMEOUT);
        var juniorEmployeeId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var body = new AddToJuniorRegistryBody();
        body.setMentorId(testData.employees.get(TestEmployees.FMS_Manager_Jawad_Mcghee));
        body.setBudgetingAccount(testData.ba_RND());
        body.setRole("Java Developer");
        StepVerifier
                .create(service.addToRegistry(auth, juniorEmployeeId, body))
                .expectNext(juniorEmployeeId)
                .verifyComplete();

        var jun = juniorRepo.findById(juniorEmployeeId).block(MONO_DEFAULT_TIMEOUT);
        Assertions.assertEquals(body.getMentorId(), jun.getMentorId());
        Assertions.assertEquals(body.getBudgetingAccount(), jun.getBudgetingAccount());
        Assertions.assertEquals(body.getRole(), jun.getRole());
        Assertions.assertEquals(auth.getEmployeeInfo().getEmployeeId(), jun.getCreatedBy());
    }

    private Mono<Void> cleanTables() {
        return db.sql("delete from udr.junior_report").then()
                .then(db.sql("delete from udr.junior_registry").then());
    }

    private Mono<JuniorEntry> buildJunior(int juniorId, Integer mentorId) {
        var entry = new JuniorEntry();
        entry.setJuniorId(juniorId);
        entry.setMentorId(mentorId);
        entry.setBudgetingAccount(testData.ba_RND());
        entry.setCreatedAt(dateTimeService.now());
        entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        entry.setRole("Java Developer");
        return juniorRepo.save(entry);
    }

    private Mono<JuniorReportEntry> buildReport(int juniorId) {
        var entry = new JuniorReportEntry();
        entry.setJuniorId(juniorId);
        entry.setComment("some comment");
        entry.setProgress((short) 1);
        entry.setCreatedAt(dateTimeService.now());
        entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        return reportRepo.save(entry);
    }
}
