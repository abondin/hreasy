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
import ru.abondin.hreasy.platform.repo.employee.EmployeeAuthDomainService;

import java.util.Collection;

@Component
@RequiredArgsConstructor
@Slf4j
public class EmployeeUserContextMapperAdapter implements UserDetailsContextMapper {
    private final EmployeeAuthDomainService employeeAuthDomainService;

    private UserDetailsContextMapper delegate = new LdapUserDetailsMapper();

    @Override
    public void mapUserToContext(UserDetails user, DirContextAdapter ctx) {
        delegate.mapUserToContext(user, ctx);
    }

    public UserDetails mapUserFromContext(DirContextOperations ctx, String username, Collection<? extends GrantedAuthority> authorities) {
        var user = delegate.mapUserFromContext(ctx, username, authorities);
        // FIXME One more block operation to support NOT reactive Spring API
        var employeeAuthInfoEntry = employeeAuthDomainService.findIdByEmail(username).block();
        return new UserDetailsWithEmployeeInfo(user,
                employeeAuthInfoEntry.getId(),
                employeeAuthInfoEntry.getDepartmentId(),
                employeeAuthInfoEntry.getCurrentProjectId(),
                employeeAuthInfoEntry.getAccessibleDepartments(),
                employeeAuthInfoEntry.getAccessibleProjects());
    }
}
