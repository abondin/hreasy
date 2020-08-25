package ru.abondin.hreasy.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.TestDataContainer;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.config.HrEasySecurityProps;
import ru.abondin.hreasy.platform.repo.SqlServerContextInitializer;

import java.time.Duration;

// Test Data Generation is broken now
@ActiveProfiles({"test", "dev"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {SqlServerContextInitializer.class})
@Slf4j
public class EmployeeServiceTest {

    private final static Duration MONO_DEFAULT_TIMEOUT = Duration.ofSeconds(3);


    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AuthHandler authHandler;

    @Autowired
    private TestDataContainer testData;

    @Autowired
    private HrEasySecurityProps securityProps;

    private AuthContext auth;

    @BeforeEach
    protected void validateTestConfiguration() {
        if (securityProps.getMasterPassword().isBlank()) {
            Assertions.fail("No master password found");
        }
        this.auth = auth(TestEmployees.Admin_Shaan_Pitts).block(MONO_DEFAULT_TIMEOUT);
        testData.initAsync().block(MONO_DEFAULT_TIMEOUT);
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
                        .flatMap(ctx -> employeeService.updateCurrentProject(testData.projects.get("M1 Billing"), ctx)))
                .expectNext(true)
                .verifyComplete();
    }

    @Test
    @DisplayName("Move FMS_Empl_Jenson_Curtis from FMS to Policy Manager by FMS_Empl_Ammara_Knott")
    public void testUpdateProjectForAnotherEmployee() {
        //
        var jensonId = testData.employees.get(TestEmployees.FMS_Empl_Jenson_Curtis);
        var ctx = auth(TestEmployees.FMS_Empl_Ammara_Knott).block(MONO_DEFAULT_TIMEOUT);
        StepVerifier
                .create(employeeService.updateCurrentProject(jensonId, testData.projects.get("M1 Policy Manager"), ctx))
                .expectError(AccessDeniedException.class).verify(MONO_DEFAULT_TIMEOUT);
    }


    private Mono<AuthContext> auth(String username) {
        return authHandler.login(new UsernamePasswordAuthenticationToken(username, securityProps.getMasterPassword()));
    }
}
