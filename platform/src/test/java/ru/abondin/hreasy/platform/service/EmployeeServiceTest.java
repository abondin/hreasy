package ru.abondin.hreasy.platform.service;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
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
import ru.abondin.hreasy.platform.repo.TestDataGenerator;

// Test Data Generation is broken now
@Disabled
@ActiveProfiles({"test", "dev"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {SqlServerContextInitializer.class})
@Slf4j
public class EmployeeServiceTest {
    @Autowired
    private TestDataGenerator testDataGenerator;
    @Autowired
    private EmployeeService employeeService;

    @Autowired
    private AuthHandler authHandler;

    @Autowired
    private HrEasySecurityProps securityProps;

    private AuthContext auth;

    @BeforeEach
    protected void generateData() {
        if (securityProps.getMasterPassword().isBlank()) {
            Assertions.fail("No master password found");
        }
        testDataGenerator.generate().then(
                authHandler
                        .login(new UsernamePasswordAuthenticationToken("Ivan.Ivanov", securityProps.getMasterPassword())).doOnSuccess(a -> {
                            auth = a;
                        }
                )
        ).block();
    }

    @Test
    public void testFindNotFired() {
        /* We have 5 not fired employees in database*/
        StepVerifier
                .create(employeeService.findAll(auth, false))
                .expectNextCount(5)
                .verifyComplete();
    }

    @Test
    public void testFindAllEmployees() {
        /* We have 5 not fired employees +1 employee in database*/
        StepVerifier
                .create(employeeService.findAll(auth, true))
                .expectNextCount(6)
                .verifyComplete();
    }

}
