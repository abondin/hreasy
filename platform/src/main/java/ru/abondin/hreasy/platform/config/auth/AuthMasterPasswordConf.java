package ru.abondin.hreasy.platform.config.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import ru.abondin.hreasy.platform.auth.DbAuthoritiesPopulator;
import ru.abondin.hreasy.platform.auth.MasterPasswordAuthenticationProvider;
import ru.abondin.hreasy.platform.config.HrEasySecurityProps;
import ru.abondin.hreasy.platform.repo.employee.EmployeeAuthDomainService;

/**
 * @see MasterPasswordAuthenticationProvider
 */
@Configuration
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${hreasy.web.sec.master-password:}')")
@Slf4j
public class AuthMasterPasswordConf {
    @Bean("masterPasswordAuthenticationManager")
    ReactiveAuthenticationManager masterPasswordAuthenticationManager(
            final HrEasySecurityProps securityProps,
            final DbAuthoritiesPopulator authoritiesPopulator,
            final EmployeeAuthDomainService employeeAuthDomainService
    ) {
        log.warn("""
                            
                ----------------------------
                Master Password Authentication Provider is enabled.
                Please double sure that you in development environment, not in production.
                ----------------------------
                            
                """);
        return new MasterPasswordAuthenticationProvider(
                securityProps, authoritiesPopulator, employeeAuthDomainService
        );
    }
}
