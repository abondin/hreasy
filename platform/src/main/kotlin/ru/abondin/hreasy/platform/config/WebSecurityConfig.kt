package ru.abondin.hreasy.platform.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.format.Formatter
import org.springframework.format.FormatterRegistry
import org.springframework.ldap.core.support.BaseLdapPathContextSource
import org.springframework.ldap.core.support.LdapContextSource
import org.springframework.security.authentication.DelegatingReactiveAuthenticationManager
import org.springframework.security.authentication.ProviderManager
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.ReactiveAuthenticationManagerAdapter
import org.springframework.security.config.web.server.ServerHttpSecurity
import org.springframework.security.ldap.authentication.BindAuthenticator
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch
import org.springframework.security.ldap.search.LdapUserSearch
import org.springframework.security.web.server.SecurityWebFilterChain
import org.springframework.security.web.server.context.ServerSecurityContextRepository
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.reactive.config.WebFluxConfigurer
import ru.abondin.hreasy.platform.api.GlobalWebErrorsHandler
import ru.abondin.hreasy.platform.sec.DbAuthoritiesPopulator
import ru.abondin.hreasy.platform.sec.EmployeeUserContextMapperAdapter
import ru.abondin.hreasy.platform.sec.MasterPasswordAuthenticationProvider
import java.time.LocalDate
import java.time.format.DateTimeFormatter
import java.util.*


@Configuration
class WebSecurityConfig(val prop: LdapConfigurationProperties) {
    @Bean
    fun authenticationManager(contextSource: BaseLdapPathContextSource,
                              userSearch: LdapUserSearch,
                              dbAuthoritiesPopulator: DbAuthoritiesPopulator,
                              securityProps: HrEasySecurityProps,
                              masterPasswordAuthenticationProvider: MasterPasswordAuthenticationProvider,
                              employeeUserContextMapperAdapter: EmployeeUserContextMapperAdapter): ReactiveAuthenticationManager? {
        val ba = BindAuthenticator(contextSource);
        ba.setUserSearch(userSearch);
        val providers = arrayListOf<ReactiveAuthenticationManager>()
        if (securityProps.masterPassword.isNotBlank()) {
            providers.add(masterPasswordAuthenticationProvider);
        }
        val ldapProvider = LdapAuthenticationProvider(ba, dbAuthoritiesPopulator);
        ldapProvider.setUserDetailsContextMapper(employeeUserContextMapperAdapter);
        providers.add(ReactiveAuthenticationManagerAdapter(ProviderManager(listOf(ldapProvider))));
        return DelegatingReactiveAuthenticationManager(providers);
    }

    @Bean
    fun serverSecurityContextRepository(): ServerSecurityContextRepository? {
        return WebSessionServerSecurityContextRepository()
    }

    @Bean
    fun securityWebFilterChain(http: ServerHttpSecurity,
                               securityContextRepository: ServerSecurityContextRepository?,
                               errorHandler: GlobalWebErrorsHandler
    ): SecurityWebFilterChain? {
        return http
                .authorizeExchange()
                .pathMatchers("/api/v1/login", "/actuator/**").permitAll()
                .anyExchange().authenticated()
                .and()
                .csrf().disable()
                .formLogin().disable()
                .logout().disable()
                .httpBasic().securityContextRepository(securityContextRepository)
                .and().exceptionHandling().accessDeniedHandler(errorHandler).authenticationEntryPoint(errorHandler)
                .and().build()
    }


    @Bean
    fun contextSource(): BaseLdapPathContextSource? {
        val ctx = LdapContextSource();
        ctx.setUrl(prop.serverUrl);
        if (!prop.userDn.isBlank()) {
            ctx.userDn = prop.userDn;
            ctx.password = prop.userPassword;
        }
        return ctx
    }

    @Bean
    fun searchFilter(contextSource: BaseLdapPathContextSource): LdapUserSearch = FilterBasedLdapUserSearch(prop.searchBase, prop.searchFilter, contextSource);
}


data class AuthContext(
        val username: String,
        val email: String,
        val authorities: Collection<String>,
        val employeeInfo: EmployeeInfo?) {
    data class EmployeeInfo(val id: Int);
}


/**
 * Deals with local date and local datetime
 */
@Component
class CustomWebFluxConfigurer : WebFluxConfigurer {
    class LocalDateFormatter : Formatter<LocalDate> {
        override fun print(date: LocalDate, locale: Locale): String {
            return DateTimeFormatter.ISO_DATE.withLocale(locale).format(date)
        }

        override fun parse(str: String, locale: Locale): LocalDate {
            return LocalDate.parse(str, DateTimeFormatter.ISO_DATE.withLocale(locale))
        }
    }

    override fun addFormatters(registry: FormatterRegistry) {
        registry.addFormatter(LocalDateFormatter())
    }
}




