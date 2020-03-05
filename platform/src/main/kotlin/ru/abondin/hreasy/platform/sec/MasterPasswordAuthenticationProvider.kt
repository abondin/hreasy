package ru.abondin.hreasy.platform.sec

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.Authentication
import org.springframework.security.core.userdetails.User
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono
import ru.abondin.hreasy.platform.config.HrEasySecurityProps
import ru.abondin.hreasy.platform.repo.EmployeeRepo

/**
 * For development purposes only.
 * Allow any user with valid employee email with fixed master password
 */
@Component
class MasterPasswordAuthenticationProvider(
        private val securityProps: HrEasySecurityProps,
        private val authoritiesPopulator: DbAuthoritiesPopulator,
        private val employeeRepo: EmployeeRepo
) : ReactiveAuthenticationManager {

    override fun authenticate(authentication: Authentication): Mono<Authentication> {
        val inputAuth = authentication as UsernamePasswordAuthenticationToken;
        val username = inputAuth.name;
        val password = inputAuth.credentials as String
        if (securityProps.masterPassword != password) {
            // throw BadCredentialsException("Master password is invalid");
            // Move control to default LDAP provider
            return Mono.empty();
        }
        val email = AuthHandler.emailFromUsername(username);
        return employeeRepo.findIdByEmail(email)
                .flatMap { employeeId ->
                    authoritiesPopulator.getGrantedAuthorities(username).collectList()
                            .map { authorities ->
                                val user = UserDetailsWithEmployeeInfo(User(
                                        username,
                                        "",
                                        authorities), employeeId);
                                val result = UsernamePasswordAuthenticationToken(user, password, authorities);
                                result.details = inputAuth.details;
                                result.eraseCredentials();
                                return@map result;
                            }
                }
    }
}