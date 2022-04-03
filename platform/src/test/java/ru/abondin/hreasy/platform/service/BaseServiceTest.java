package ru.abondin.hreasy.platform.service;

import org.junit.jupiter.api.Assertions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.TestDataContainer;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.config.HrEasySecurityProps;

import java.time.Duration;

public abstract class BaseServiceTest {

    protected final static Duration MONO_DEFAULT_TIMEOUT = Duration.ofSeconds(15);

    @Autowired
    protected AuthHandler authHandler;

    @Autowired
    protected TestDataContainer testData;

    @Autowired
    protected HrEasySecurityProps securityProps;

    protected AuthContext auth;

    protected void initEmployeesDataAndLogin() {
        if (securityProps.getMasterPassword().isBlank()) {
            Assertions.fail("No master password found");
        }
        this.auth = auth(TestEmployees.Admin_Shaan_Pitts).block(MONO_DEFAULT_TIMEOUT);
        testData.initAsync().block(MONO_DEFAULT_TIMEOUT);
    }

    protected Mono<AuthContext> auth(String username) {
        return authHandler.login(new UsernamePasswordAuthenticationToken(
                TestDataContainer.emailFromUserName(username),
                securityProps.getMasterPassword())
        );
    }
}
