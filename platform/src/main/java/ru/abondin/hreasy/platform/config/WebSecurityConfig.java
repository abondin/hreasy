package ru.abondin.hreasy.platform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import ru.abondin.hreasy.platform.api.GlobalWebErrorsHandler;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebSecurityConfig {

    @Autowired(required = false)
    @Qualifier("ldapAuthenticationManager")
    private ReactiveAuthenticationManager ldapAuthenticationManager;

    @Autowired(required = false)
    @Qualifier("masterPasswordAuthenticationManager")
    private ReactiveAuthenticationManager masterPasswordAuthenticationManager;

    @Autowired(required = false)
    @Qualifier("internalPasswordAuthenticationManager")
    private ReactiveAuthenticationManager internalPasswordAuthenticationManager;

    @Bean
    ReactiveAuthenticationManager authenticationManager() {
        var authenticationManagers = new ArrayList<ReactiveAuthenticationManager>();
        log.info("Collecting authentication managers...");
        Assert.isTrue(!(internalPasswordAuthenticationManager != null && ldapAuthenticationManager != null),
                "Internal Password cannot be enabled with LDAP at the same time." +
                        " Set HREASY_WEB_SEC_INTERNAL-PASSWORD-ENABLED to false" +
                        " or HREASY_LDAP_SERVER-URL to empty");
        addManager(authenticationManagers, "Master Password", masterPasswordAuthenticationManager);
        addManager(authenticationManagers, "Internal Password", internalPasswordAuthenticationManager);
        addManager(authenticationManagers, "LDAP", ldapAuthenticationManager);
        Assert.notEmpty(authenticationManagers, "There are no authentication managers found");
        return new DelegatingReactiveAuthenticationManager(authenticationManagers);
    }

    private void addManager(List<ReactiveAuthenticationManager> managers, String name, @Nullable ReactiveAuthenticationManager manager) {
        if (manager == null) {
            log.info("Collecting authentication managers: {} NOT found", name);
        } else {
            log.info("Collecting authentication managers: {} found", name);
            managers.add(manager);
        }
    }

    @Bean
    ServerSecurityContextRepository serverSecurityContextRepository() {
        return new WebSessionServerSecurityContextRepository();
    }

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                  ServerSecurityContextRepository securityContextRepository,
                                                  GlobalWebErrorsHandler errorHandler
    ) {
        return http
                .authorizeExchange()
                .pathMatchers(
                        "/api/v1/login",
                        "/api/v1/logout",
                        "/api/v1/fs/**",
                        "/actuator/**",
                        "/favicon.ico").permitAll()
                .anyExchange().authenticated()
                .and()
                .csrf().disable()
                .securityContextRepository(securityContextRepository)
                .exceptionHandling().accessDeniedHandler(errorHandler).authenticationEntryPoint(errorHandler)
                .and().build();
    }


    /**
     * Deals with local date and local datetime
     */
    @Component
    public static class CustomWebFluxConfigurer implements WebFluxConfigurer {
        public static class LocalDateFormatter implements Formatter<LocalDate> {
            @Override
            public String print(LocalDate date, Locale locale) {
                return DateTimeFormatter.ISO_DATE.withLocale(locale).format(date);
            }

            @Override
            public LocalDate parse(String str, Locale locale) {
                return LocalDate.parse(str, DateTimeFormatter.ISO_DATE.withLocale(locale));
            }
        }

        @Override
        public void addFormatters(FormatterRegistry registry) {
            registry.addFormatter(new LocalDateFormatter());
        }
    }

}



