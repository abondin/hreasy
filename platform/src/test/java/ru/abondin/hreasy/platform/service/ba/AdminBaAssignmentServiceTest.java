package ru.abondin.hreasy.platform.service.ba;

import lombok.extern.slf4j.Slf4j;
import org.hamcrest.MatcherAssert;
import org.hamcrest.Matchers;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountAssignmentRepo;
import ru.abondin.hreasy.platform.service.BaseServiceTest;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.TestFixedDataTimeConfig;
import ru.abondin.hreasy.platform.service.admin.ba.AdminBaAssignmentService;
import ru.abondin.hreasy.platform.service.admin.ba.dto.CreateBusinessAccountAssignmentBody;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.OffsetDateTime;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.comparesEqualTo;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Import(TestFixedDataTimeConfig.class)
@Slf4j
public class AdminBaAssignmentServiceTest extends BaseServiceTest {

    @Autowired
    private DateTimeService dateTimeService;

    @Autowired
    private BusinessAccountAssignmentRepo assignmentRepo;

    @Autowired
    private AdminBaAssignmentService assignmentService;

    @BeforeEach
    protected void beforeEach() {
        var now = OffsetDateTime.now();
        ((TestFixedDataTimeConfig.TestFixedDataTimeService) dateTimeService).init(now);

        initEmployeesDataAndLogin();
        clearAllAssignments();
    }

    @Test
    public void testCreateAssignmentWithAllFields(){
        var body = new CreateBusinessAccountAssignmentBody();
        var baId = testData.bas.get("Billing");
        var projectId = testData.projects.get("M1 Billing");
        var employeeId =  testData.employees.get(TestEmployees.Billing_Empl_Haiden_Spooner);
        var period = 2022;
        var position = "Java Developer";
        body.setEmployee(employeeId);
        body.setProject(projectId);
        body.setStartDate(dateTimeService.now().toLocalDate().minusMonths(1));
        body.setEndDate(dateTimeService.now().toLocalDate().plusMonths(1));
        body.setBaPosition(position);
        body.setBaPositionRate(BigDecimal.valueOf(100000.0));
        body.setBaPositionRateFactor(BigDecimal.valueOf(0.9));
        body.setEmploymentRate(BigDecimal.valueOf(110000.0));
        body.setEmploymentRateFactor(BigDecimal.valueOf(0.5));
        body.setComment("0.5 employee = 0,9 ba position");
        StepVerifier.create(assignmentService.create(auth, baId, period, body)
                .thenMany(assignmentService.findInBusinessAccount(auth, baId, period)))
                .assertNext(created->{
                    Assertions.assertEquals(baId, created.getBusinessAccount().getId());
                    Assertions.assertEquals(period, created.getPeriod());
                    Assertions.assertEquals(employeeId, created.getEmployee().getId());
                    Assertions.assertEquals(position, created.getBaPosition());
                    Assertions.assertEquals(body.getComment(), created.getComment());
                    Assertions.assertEquals(body.getStartDate(), created.getStartDate());
                    Assertions.assertEquals(body.getEndDate(), created.getEndDate());
                    assertThat(body.getBaPositionRate(), comparesEqualTo(created.getBaPositionRate()));
                    assertThat(body.getBaPositionRateFactor(), comparesEqualTo(created.getBaPositionRateFactor()));
                    assertThat(body.getEmploymentRate(), comparesEqualTo(created.getEmploymentRate()));
                    assertThat(body.getEmploymentRateFactor(), comparesEqualTo(created.getEmploymentRateFactor()));
                    Assertions.assertNull(created.getAncestorAssignment());
                    Assertions.assertNull(created.getClosedAt());
                    Assertions.assertNull(created.getClosedComment());
                    Assertions.assertNull(created.getClosedBy());
                })
                .verifyComplete();
    }


    private void clearAllAssignments() {
        assignmentRepo.deleteAll().block(MONO_DEFAULT_TIMEOUT);
    }

}
