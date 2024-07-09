package ru.abondin.hreasy.platform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.config.web.server.SecurityWebFiltersOrder;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.AuthenticationWebFilter;
import org.springframework.security.web.server.context.NoOpServerSecurityContextRepository;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.security.web.server.util.matcher.ServerWebExchangeMatchers;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import ru.abondin.hreasy.platform.api.GlobalWebErrorsHandler;
import ru.abondin.hreasy.platform.config.telegram.TelegramJwtAuthenticationConverter;
import ru.abondin.hreasy.platform.tg.TgAuthLogService;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebSecurityConfig {


    @Bean
    ReactiveAuthenticationManager authenticationManager(
            @Autowired(required = false)
            @Qualifier("ldapAuthenticationManager")
            ReactiveAuthenticationManager ldapAuthenticationManager,

            @Autowired(required = false)
            @Qualifier("masterPasswordAuthenticationManager")
            ReactiveAuthenticationManager masterPasswordAuthenticationManager,

            @Autowired(required = false)
            @Qualifier("internalPasswordAuthenticationManager")
            ReactiveAuthenticationManager internalPasswordAuthenticationManager
    ) {
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
    @Order(2)
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http,
                                                  ServerSecurityContextRepository securityContextRepository,
                                                  GlobalWebErrorsHandler errorHandler
    ) {
        return http
                .authorizeExchange(authorizeExchangeSpec -> authorizeExchangeSpec.
                        // Allow some methods without any authentication
                                pathMatchers(
                                "/api/v1/login",
                                "/api/v1/logout",
                                "/api/v1/fs/**",
                                "/actuator/**",
                                "/favicon.ico",
                                "/api/v1/telegram/confirm/**").permitAll()
                        // Allow api methods for web interface only for Users, logged in web
                        .pathMatchers("/api/**")
                        .authenticated()
                )
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .securityContextRepository(securityContextRepository)
                .exceptionHandling(exSpec -> exSpec
                        .accessDeniedHandler(errorHandler)
                        .authenticationEntryPoint(errorHandler))
                .build();
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

    // region Internal API

    /**
     * Isolated authentication manager for internal api
     *
     * @param http
     * @return
     */
    @Bean
    @Order(1)
    public SecurityWebFilterChain internalApiSecurityWebFilterChain(
            ServerHttpSecurity http,
            GlobalWebErrorsHandler errorHandler,
            ServerSecurityContextRepository securityContextRepository,
            TelegramJwtAuthenticationConverter telegramJwtAuthenticationConverter,
            TgAuthLogService tgAuthLogService
    ) {
        return http.securityMatcher(ServerWebExchangeMatchers.pathMatchers("/telegram/**"))
                .authorizeExchange(exchanges ->
                        exchanges.pathMatchers("/telegram/api/v1/confirm/start")
                                .authenticated()
                                .anyExchange().hasAuthority(TelegramJwtAuthenticationConverter.TELEGRAM_CONFIRMED_RESERVED_AUTHORITY)
                )
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .anonymous(ServerHttpSecurity.AnonymousSpec::disable)
                .securityContextRepository(NoOpServerSecurityContextRepository.getInstance())
                .exceptionHandling(exSpec -> exSpec
                        .accessDeniedHandler(errorHandler)
                        .authenticationEntryPoint(errorHandler))
                .addFilterAt(telegramAuthenticationWebFilter(
                        securityContextRepository,
                        telegramJwtAuthenticationConverter,
                        tgAuthLogService
                ), SecurityWebFiltersOrder.AUTHENTICATION)
                .build();
    }

    private AuthenticationWebFilter telegramAuthenticationWebFilter(
            ServerSecurityContextRepository securityContextRepository,
            TelegramJwtAuthenticationConverter jwtServerAuthenticationConverter,
            TgAuthLogService tgAuthLogService) {
        AuthenticationWebFilter authenticationWebFilter = new AuthenticationWebFilter(telegramAuthenticationManager(tgAuthLogService));
        authenticationWebFilter.setServerAuthenticationConverter(jwtServerAuthenticationConverter);
        authenticationWebFilter.setSecurityContextRepository(securityContextRepository);
        return authenticationWebFilter;
    }

    private ReactiveAuthenticationManager telegramAuthenticationManager(TgAuthLogService tgAuthLogService) {
        return authentication ->
                tgAuthLogService.log(authentication).thenReturn(authentication);
    }

    // endregion

}



