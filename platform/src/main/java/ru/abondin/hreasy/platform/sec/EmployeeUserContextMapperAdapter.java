package ru.abondin.hreasy.platform.sec;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ldap.core.DirContextAdapter;
import org.springframework.ldap.core.DirContextOperations;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.ldap.userdetails.LdapUserDetailsMapper;
import org.springframework.security.ldap.userdetails.UserDetailsContextMapper;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeUserContextMapperAdapter implements UserDetailsContextMapper {
    private final EmployeeRepo employeeRepo;

    private UserDetailsContextMapper delegate = new LdapUserDetailsMapper();

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        delegate.mapUserToContext(user, ctx);
    }

    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        var user = delegate.mapUserFromContext(ctx, username, authorities);
        var email = AuthHandler.emailFromUsername(username);
        // FIXME One more block operation to support NOT reactive Spring API
        var employeeId = employeeRepo.findIdByEmail(email).block();
        return new UserDetailsWithEmployeeInfo(user, employeeId);
    }
}
