package ru.abondin.hreasy.platform.sec

import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.context.ReactiveSecurityContextHolder
import org.springframework.security.core.context.SecurityContext
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.security.web.context.HttpSessionSecurityContextRepository
import org.springframework.stereotype.Component
import org.springframework.web.server.WebSession
import reactor.core.publisher.Mono
import ru.abondin.hreasy.platform.config.AuthContext
import ru.abondin.hreasy.platform.config.HrEasySecurityProps

/**
 * Helper for auth
 */
@Component
class AuthHandler(private val authenticationManager: ReactiveAuthenticationManager,
                  private val securityProps: HrEasySecurityProps) {


    fun login(authenticationToken: UsernamePasswordAuthenticationToken, webSession: WebSession): Mono<LoginResponse> {
        return authenticationManager.authenticate(authenticationToken).flatMap { authResult ->
            ReactiveSecurityContextHolder.getContext().map { securityContext ->
                securityContext.authentication = authResult;
                return@map securityContext;
            }.subscriberContext(ReactiveSecurityContextHolder.withAuthentication(authResult))
        }.flatMap { securityContext ->
            webSession.attributes.put(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext)
            return@flatMap webSession.save()
        }.then(
                currentAuth()
        ).map { auth ->
            LoginResponse(CurrentUserDto(auth))
        };
    }

    fun login(authenticationToken: UsernamePasswordAuthenticationToken): Mono<AuthContext> {
        return authenticationManager.authenticate(authenticationToken).flatMap { authResult ->
            ReactiveSecurityContextHolder.getContext().map { securityContext ->
                securityContext.authentication = authResult;
                return@map securityContext;
            }.subscriberContext(ReactiveSecurityContextHolder.withAuthentication(authResult))
        }.map { context ->
            buildFromSecurityContext(context);
        }
    }

    fun logout(webSession: WebSession): Mono<LogoutResponse> {
        webSession.getAttributes().remove(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        return webSession.invalidate().map { LogoutResponse() };
    }

    /**
     * Get current auth info from Spring Reactive Security Context
     */
    companion object {
        //FIXME Get from securityProps.emailSuffix
        val emailSuffix = "@stm-labs.ru";

        fun currentAuth(): Mono<AuthContext> =
                ReactiveSecurityContextHolder
                        .getContext()
                        .map { buildFromSecurityContext(it) }

        /**
         * Get employee email from username
         */
        fun emailFromUsername(username: String): String {
            return "${username}${emailSuffix}";
        }

        private fun buildFromSecurityContext(context: SecurityContext): AuthContext {
            val userDetails = context.authentication.principal as UserDetails;
            val email = emailFromUsername(userDetails.username);
            val employee: AuthContext.EmployeeInfo?;
            employee = if (userDetails is UserDetailsWithEmployeeInfo && userDetails.employeeId != null) {
                AuthContext.EmployeeInfo(userDetails.employeeId);
            } else {
                null;
            }
            return AuthContext(userDetails.username, email, userDetails.authorities.map { a -> a.authority }, employee);
        }
    }


}


data class LoginResponse(
        val currentUser: CurrentUserDto
);
data class LogoutResponse(
        val status: String = "OK"
);

data class CurrentUserDto(
        val username: String,
        val email: String,
        val authorities: Collection<String>,
        val employee: EmployeeInfo?
) {
    constructor(authContext: AuthContext) : this(
            authContext.username,
            authContext.email,
            authContext.authorities,
            if (authContext.employeeInfo == null) null else EmployeeInfo(authContext.employeeInfo)
    )

    data class EmployeeInfo(val id: Int) {
        constructor(employeeInfo: AuthContext.EmployeeInfo) : this(employeeInfo.id);
    }
}