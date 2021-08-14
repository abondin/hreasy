package ru.abondin.hreasy.platform.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.sec.SecurityService;

import java.util.Collection;

/**
 * Setup authorities for user from cookie or database
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class DbAuthoritiesPopulator implements LdapAuthoritiesPopulator {
    private final SecurityService securityService;

    @Override
    public Collection<? extends GrantedAuthority> getGrantedAuthorities(DirContextOperations userData,
                                                                        String username) {
        return getGrantedAuthoritiesBlocked(username);
    }

    private Collection<? extends GrantedAuthority> getGrantedAuthoritiesBlocked(String username) {
        return securityService
                .getPermissions(username)
                .map(perm -> new SimpleGrantedAuthority(perm))
                .collectList()
                //FIXME blocking code to support not reactive LdapAuthoritiesPopulator
                .blockOptional().get();
    }

    public Flux<GrantedAuthority> getGrantedAuthorities(String username) {
        return securityService
                .getPermissions(username)
                .map(perm -> new SimpleGrantedAuthority(perm));
    }
}
