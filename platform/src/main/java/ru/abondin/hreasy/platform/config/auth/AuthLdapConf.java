package ru.abondin.hreasy.platform.config.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.ldap.core.support.BaseLdapPathContextSource;
import org.springframework.ldap.core.support.LdapContextSource;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.ReactiveAuthenticationManagerAdapter;
import org.springframework.security.ldap.authentication.BindAuthenticator;
import org.springframework.security.ldap.authentication.LdapAuthenticationProvider;
import org.springframework.security.ldap.search.FilterBasedLdapUserSearch;
import org.springframework.security.ldap.search.LdapUserSearch;
import ru.abondin.hreasy.platform.auth.DbAuthoritiesPopulator;
import ru.abondin.hreasy.platform.config.LdapConfigurationProperties;
import ru.abondin.hreasy.platform.sec.EmployeeUserContextMapperAdapter;

import java.util.Arrays;

/**
 * LDAP security configuration
 */
@Configuration
@ConditionalOnExpression("!T(org.springframework.util.StringUtils).isEmpty('${hreasy.ldap.server-url:}')")
@RequiredArgsConstructor
@Slf4j
public class AuthLdapConf {

    private final LdapConfigurationProperties prop;

    @Bean("ldapAuthenticationManager")
    ReactiveAuthenticationManager ldapAuthenticationManager(BaseLdapPathContextSource contextSource,
                                                            LdapUserSearch userSearch,
                                                            DbAuthoritiesPopulator dbAuthoritiesPopulator,
                                                            EmployeeUserContextMapperAdapter employeeUserContextMapperAdapter) {
        log.info("Initializing LDAP Authentication Manager for {}", prop.getServerUrl());
        var ba = new BindAuthenticator(contextSource);
        ba.setUserSearch(userSearch);
        var ldapProvider = new LdapAuthenticationProvider(ba, dbAuthoritiesPopulator);
        ldapProvider.setUserDetailsContextMapper(employeeUserContextMapperAdapter);
        return new ReactiveAuthenticationManagerAdapter(new ProviderManager(Arrays.asList(ldapProvider)));
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
}
