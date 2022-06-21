package ru.abondin.hreasy.platform.config.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import ru.abondin.hreasy.platform.auth.DbAuthoritiesPopulator;
import ru.abondin.hreasy.platform.auth.InternalPasswordAuthenticationProvider;
import ru.abondin.hreasy.platform.repo.employee.EmployeeAuthDomainService;

/**
 * @see InternalPasswordAuthenticationProvider
 */
@Configuration
@ConditionalOnProperty(value = "hreasy.web.sec.internal-password-enabled", matchIfMissing = true)
@Slf4j
public class AuthInternalPasswordConf {
    @Bean("internalPasswordAuthenticationManager")
    ReactiveAuthenticationManager masterPasswordAuthenticationManager(
            final DbAuthoritiesPopulator authoritiesPopulator,
            final EmployeeAuthDomainService employeeAuthDomainService
    ) {
        return new InternalPasswordAuthenticationProvider(
                authoritiesPopulator, employeeAuthDomainService
        );
    }
}
