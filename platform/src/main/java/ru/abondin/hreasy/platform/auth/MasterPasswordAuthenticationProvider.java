package ru.abondin.hreasy.platform.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.config.HrEasySecurityProps;

/**
 * For development purposes only.
 * Allow any user with valid employee email with fixed master password
 */
@RequiredArgsConstructor
@Slf4j
public class MasterPasswordAuthenticationProvider implements ReactiveAuthenticationManager {
    private final HrEasySecurityProps securityProps;
    private final EmployeeBasedUserDetailsService userDetailsService;

    @Override
    public Mono<Authentication> authenticate(Authentication authentication) {
        var inputAuth = (UsernamePasswordAuthenticationToken) authentication;
        var username = StringUtils.trimToEmpty(inputAuth.getName());
        var password = inputAuth.getCredentials();
        if (StringUtils.isBlank(securityProps.getMasterPassword()) || !securityProps.getMasterPassword().equals(password)) {
            // throw BadCredentialsException("Master password is invalid");
            // Move control to default LDAP provider
            return Mono.empty();
        }
        return userDetailsService.findForMasterPassword(username)
                .switchIfEmpty(Mono.error(new BusinessError("errors.no.employee.found", username)))
                .map(user -> {
                    var result = new UsernamePasswordAuthenticationToken(user, password, user.getAuthorities());
                    result.setDetails(inputAuth.getDetails());
                    result.eraseCredentials();
                    return result;
                });

    }

}
