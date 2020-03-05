package ru.abondin.hreasy.platform.sec

import org.springframework.ldap.core.DirContextAdapter
import org.springframework.ldap.core.DirContextOperations
import org.springframework.security.core.GrantedAuthority
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper
import org.springframework.stereotype.Component
import ru.abondin.hreasy.platform.repo.EmployeeRepo

/**
 * Add employeeId to the UserDetails
 */
@Component
class EmployeeUserContextMapperAdapter(private val employeeRepo: EmployeeRepo) : UserDetailsContextMapper {

    private val delegate: UserDetailsContextMapper = LdapUserDetailsMapper();

    override fun mapUserToContext(user: UserDetails?, ctx: DirContextAdapter?) {
        delegate.mapUserToContext(user, ctx);
    }

    override fun mapUserFromContext(ctx: DirContextOperations, username: String, authorities: MutableCollection<out GrantedAuthority>): UserDetails {
        val user = delegate.mapUserFromContext(ctx, username, authorities);
        val email = AuthHandler.emailFromUsername(username);
        // FIXME One more block operation to support NOT reactive Spring API
        val employeeId: Int? = employeeRepo.findIdByEmail(email).block();
        return UserDetailsWithEmployeeInfo(user, employeeId);
    }
}


data class UserDetailsWithEmployeeInfo(
        private val delegate: UserDetails,
        val employeeId: Int?
) : UserDetails {
    override fun getAuthorities(): MutableCollection<out GrantedAuthority> {
        return delegate.authorities;
    }

    override fun getPassword(): String {
        return delegate.password;
    }

    override fun getUsername(): String {
        return delegate.username
    }

    override fun isAccountNonExpired(): Boolean {
        return delegate.isAccountNonExpired;
    }

    override fun isAccountNonLocked(): Boolean {
        return delegate.isAccountNonLocked;
    }

    override fun isCredentialsNonExpired(): Boolean {
        return delegate.isCredentialsNonExpired;
    }

    override fun isEnabled(): Boolean {
        return delegate.isEnabled;
    }
}

