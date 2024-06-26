package ru.abondin.hreasy.platform.config.auth;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import ru.abondin.hreasy.platform.auth.EmployeeBasedUserDetailsService;
import ru.abondin.hreasy.platform.auth.MasterPasswordAuthenticationProvider;
import ru.abondin.hreasy.platform.config.HrEasySecurityProps;

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
            final EmployeeBasedUserDetailsService userDetailsService
    ) {
        log.warn("""
                            
                ----------------------------
                Master Password Authentication Provider is enabled.
                Please double sure that you in development environment, not in production.
                ----------------------------
                            
                """);
        return new MasterPasswordAuthenticationProvider(
                securityProps, userDetailsService
        );
    }
}
