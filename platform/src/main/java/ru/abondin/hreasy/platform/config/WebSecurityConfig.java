package ru.abondin.hreasy.platform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.Formatter;
import org.springframework.format.FormatterRegistry;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerAdapter;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.context.ServerSecurityContextRepository;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.config.WebFluxConfigurer;
import ru.abondin.hreasy.platform.api.GlobalWebErrorsHandler;
import ru.abondin.hreasy.platform.auth.DbAuthoritiesPopulator;
import ru.abondin.hreasy.platform.auth.MasterPasswordAuthenticationProvider;
import ru.abondin.hreasy.platform.sec.EmployeeUserContextMapperAdapter;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Locale;

@Configuration
@Slf4j
@RequiredArgsConstructor
public class WebSecurityConfig {
    private final LdapConfigurationProperties prop;

    @Bean
    ReactiveAuthenticationManager authenticationManager(BaseLdapPathContextSource contextSource,
                                                        LdapUserSearch userSearch,
                                                        DbAuthoritiesPopulator dbAuthoritiesPopulator,
                                                        HrEasySecurityProps securityProps,
                                                        MasterPasswordAuthenticationProvider masterPasswordAuthenticationProvider,
                                                        EmployeeUserContextMapperAdapter employeeUserContextMapperAdapter) {
        var ba = new BindAuthenticator(contextSource);
        ba.setUserSearch(userSearch);
        var providers = new ArrayList<ReactiveAuthenticationManager>();
        if (StringUtils.isNotBlank(securityProps.getMasterPassword())) {
            providers.add(masterPasswordAuthenticationProvider);
        }
        var ldapProvider = new LdapAuthenticationProvider(ba, dbAuthoritiesPopulator);
        ldapProvider.setUserDetailsContextMapper(employeeUserContextMapperAdapter);
        providers.add(new ReactiveAuthenticationManagerAdapter(
                new ProviderManager(Arrays.asList(ldapProvider))));
        return new DelegatingReactiveAuthenticationManager(providers);
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
                        "/api/v1/article/shared/**",
                        "/actuator/**",
                        "/favicon.ico").permitAll()
                .anyExchange().authenticated()
                .and()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .httpBasic().securityContextRepository(securityContextRepository)
                .and().exceptionHandling().accessDeniedHandler(errorHandler).authenticationEntryPoint(errorHandler)
                .and().build();
    }

    @Bean
    BaseLdapPathContextSource contextSource() {
        var ctx = new LdapContextSource();
        ctx.setUrl(prop.getServerUrl());
        if (StringUtils.isNotBlank(prop.getUserDn())) {
            ctx.setUserDn(prop.getUserDn());
            ctx.setPassword(prop.getUserPassword());
        }
        return ctx;
    }

    @Bean
    LdapUserSearch searchFilter(BaseLdapPathContextSource contextSource) {
        return new FilterBasedLdapUserSearch(prop.getSearchBase(), prop.getSearchFilter(), contextSource);
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



