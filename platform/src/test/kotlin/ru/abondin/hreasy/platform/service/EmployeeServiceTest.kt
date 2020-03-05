package ru.abondin.hreasy.platform.service

import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import reactor.test.StepVerifier
import ru.abondin.hreasy.platform.config.AuthContext
import ru.abondin.hreasy.platform.config.HrEasySecurityProps
import ru.abondin.hreasy.platform.repo.SqlServerContextInitializer
import ru.abondin.hreasy.platform.repo.TestDataGenerator
import ru.abondin.hreasy.platform.sec.AuthHandler

@ActiveProfiles("test", "dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = [SqlServerContextInitializer::class])
class EmployeeServiceTest {

    @Autowired
    private lateinit var testDataGenerator: TestDataGenerator;
    @Autowired
    private lateinit var employeeService: EmployeeService;

    @Autowired
    private lateinit var authHandler: AuthHandler;

    @Autowired
    private lateinit var securityProps: HrEasySecurityProps;

    private lateinit var auth: AuthContext;

    @BeforeEach
    fun generateData() {
        if (securityProps.masterPassword.isBlank())
            Assertions.fail<Any>("No master password found");
        testDataGenerator.generate().then(
                authHandler
                        .login(UsernamePasswordAuthenticationToken("Ivan.Ivanov", securityProps.masterPassword)).doOnSuccess { a ->
                            auth = a
                        }
        ).block();

    }

    @Test
    fun testFindNotFired() {
        /* We have 5 not fired employees in database*/
        StepVerifier
                .create(employeeService.findAll(auth))
                .expectNextCount(5)
                .verifyComplete();
    }

    @Test
    fun testFindAllEmployees() {
        /* We have 5 not fired employees +1 employee in database*/
        StepVerifier
                .create(employeeService.findAll(auth, true))
                .expectNextCount(6)
                .verifyComplete();
    }


}