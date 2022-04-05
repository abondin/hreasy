package ru.abondin.hreasy.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;

@ActiveProfiles({"test"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {PostgreSQLTestContainerContextInitializer.class})
@Slf4j
public class EmployeeServiceTest extends BaseServiceTest{

    @Autowired
    private EmployeeService employeeService;



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
    @DisplayName("Move FMS_Empl_Ammara_Knott from FMS to M1 Billing by herself")
    public void testUpdateMyProject() {
        StepVerifier
                .create(auth(TestEmployees.FMS_Empl_Ammara_Knott)
                        .flatMap(ctx -> employeeService.updateCurrentProject(testData.projects.get("M1 Billing"), ctx))
                        .doOnError(error -> {
                            log.error("-------- Unexpected error", error);
                        })
                )
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Move FMS_Empl_Jenson_Curtis from FMS to Policy Manager by FMS_Empl_Ammara_Knott")
    public void testUpdateProjectForAnotherEmployee() {
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Empl_Ammara_Knott).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(employeeService.updateCurrentProject(jensonId, testData.projects.get("M1 Policy Manager"), ctx))
                .expectError(AccessDeniedException.class).verify(MONO_DEFAULT_TIMEOUT);
    }

}
