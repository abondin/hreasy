package ru.abondin.hreasy.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeService;

import static ru.abondin.hreasy.platform.TestEmployees.FMS_Empl_Ammara_Knott;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class EmployeeServiceTest extends BaseServiceTest {

    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private AdminEmployeeService adminEmployeeService;


    @BeforeEach
    protected void beforeEach() {
        initEmployeesDataAndLogin();
    }

    @Test
    public void testFindNotFired() {
        /* We have 13 not fired employees in database*/
        StepVerifier
                .create(employeeService.findAll(auth, false))
                .expectNextCount(13)
                .verifyComplete();
    }

    @Test
    public void testFindAllEmployees() {
        /* We have 13 not fired employees +1 employee in database*/
        StepVerifier
                .create(employeeService.findAll(auth, true))
                .expectNextCount(14)
                .verifyComplete();
    }

    @Test
    @DisplayName("Update my current project")
    public void testUpdateMyProject() {
        StepVerifier
                .create(auth(FMS_Empl_Ammara_Knott)
                        .flatMap(ctx -> adminEmployeeService.updateCurrentProject(
                                testData.employees.get(FMS_Empl_Ammara_Knott)
                                , testData.updateCurrentProjectBody("M1 Billing")
                                , ctx))
                        .doOnError(error -> {
                            log.error("-------- Unexpected error", error);
                        })
                )
                .expectNextCount(1)
                .verifyComplete();
    }

    @Test
    @DisplayName("Test update project for another employee")
    public void testUpdateProjectForAnotherEmployee() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(FMS_Empl_Ammara_Knott).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(adminEmployeeService.updateCurrentProject(
                        jensonId
                        , testData.updateCurrentProjectBody("M1 Policy Manager")
                        , ctx))
                .expectError(AccessDeniedException.class).verify(MONO_DEFAULT_TIMEOUT);
    }

}
