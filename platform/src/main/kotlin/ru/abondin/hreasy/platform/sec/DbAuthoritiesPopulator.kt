package ru.abondin.hreasy.platform.sec

import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.authority.SimpleGrantedAuthority
import org.springframework.security.ldap.userdetails.LdapAuthoritiesPopulator
import org.springframework.stereotype.Component
import reactor.core.publisher.Flux

/**
 * Setup authorities for user from cookie or database
 */
@Component
class DbAuthoritiesPopulator(
        private val securityService: SecurityService
) : LdapAuthoritiesPopulator {

    override fun getGrantedAuthorities(userData: DirContextOperations?, username: String): MutableCollection<out GrantedAuthority> {
        return getGrantedAuthoritiesBlocked(username);
    }

    private fun getGrantedAuthoritiesBlocked(username: String): MutableCollection<out GrantedAuthority> {
        return securityService
                .getPermissions(AuthHandler.emailFromUsername(username))
                .map { perm -> SimpleGrantedAuthority(perm) }
                .collectList()
                //FIXME blocking code to support not reactive LdapAuthoritiesPopulator
                .blockOptional().get();
    }

    fun getGrantedAuthorities(username: String): Flux<out GrantedAuthority> {
        return securityService
                .getPermissions(AuthHandler.emailFromUsername(username))
                .map { perm -> SimpleGrantedAuthority(perm) };
    }
}