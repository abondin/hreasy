package ru.abondin.hreasy.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.config.HrEasySecurityProps;
import ru.abondin.hreasy.platform.repo.SqlServerContextInitializer;

// Test Data Generation is broken now
@ActiveProfiles({"test", "dev"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {SqlServerContextInitializer.class})
@Slf4j
public class EmployeeServiceTest {
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AuthHandler authHandler;

    @Autowired
    private HrEasySecurityProps securityProps;

    private AuthContext auth;

    @BeforeEach
    protected void validateTestConfiguration() {
        if (securityProps.getMasterPassword().isBlank()) {
            Assertions.fail("No master password found");
        }
        this.auth = authHandler.login(new UsernamePasswordAuthenticationToken("Shaan.Pitts", securityProps.getMasterPassword())).block();
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

}
