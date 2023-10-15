package ru.abondin.hreasy.platform.auth;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.stereotype.Component;
import org.springframework.web.server.WebSession;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.sec.UserDetailsWithEmployeeInfo;

import java.util.ArrayList;
import java.util.Collection;
import java.util.stream.Collectors;

/**
 * Helper for auth
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class AuthHandler {

    @Data
    @NoArgsConstructor
    public static class CurrentUserDto {
        private String username;
        private String email;
        private Collection<String> authorities = new ArrayList<>();
        private AuthContext.EmployeeInfo employee;

        public CurrentUserDto(AuthContext authContext) {
            this.username = authContext.getUsername();
            this.email = authContext.getEmail();
            this.authorities = authContext.getAuthorities();
            if (authContext.getEmployeeInfo() != null) {
                this.employee = new AuthContext.EmployeeInfo(authContext.getEmployeeInfo());
            }
        }
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LoginResponse {
        private CurrentUserDto currentUser;
    }

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class LogoutResponse {
        private String status = "OK";
    }

    public final static Mono<AuthContext> currentAuth() {
        return ReactiveSecurityContextHolder.getContext()
                .map(it -> buildFromSecurityContext(it));
    }

    private static AuthContext buildFromSecurityContext(SecurityContext context) {
        var userDetails = (UserDetails) context.getAuthentication().getPrincipal();
        var email = userDetails.getUsername();
        AuthContext.EmployeeInfo employee;
        if (userDetails instanceof UserDetailsWithEmployeeInfo && ((UserDetailsWithEmployeeInfo) userDetails).getEmployeeId() != null) {
            var withEmployeeInfo = ((UserDetailsWithEmployeeInfo) userDetails);
            employee = new AuthContext.EmployeeInfo(withEmployeeInfo.getEmployeeId(),
                    withEmployeeInfo.getDepartmentId(),
                    withEmployeeInfo.getCurrentProjectId(),
                    withEmployeeInfo.getAccessibleDepartments(),
                    withEmployeeInfo.getAccessibleBas(),
                    withEmployeeInfo.getAccessibleProjects(),
                    withEmployeeInfo.getLoggedInType());
        } else {
            employee = null;
        }
        var ctx = new AuthContext(userDetails.getUsername(), email,
                userDetails.getAuthorities().stream().map(GrantedAuthority::getAuthority).collect(Collectors.toList()),
                employee);
        log.debug("Auth context built for {}", userDetails.getUsername());
        return ctx;
    }

    private final ReactiveAuthenticationManager authenticationManager;

    public Mono<LoginResponse> login(UsernamePasswordAuthenticationToken authenticationToken, WebSession webSession) {
        return authenticationManager.authenticate(authenticationToken).flatMap(
                authResult -> ReactiveSecurityContextHolder.getContext().map(
                        securityContext -> {
                            securityContext.setAuthentication(authResult);
                            return securityContext;
                        }).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authResult))
        ).flatMap(securityContext -> {
            webSession.getAttributes().put(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY, securityContext);
            return webSession.save();
        }).then(currentAuth()).map(
                auth -> new LoginResponse(new CurrentUserDto(auth))
        );
    }

    public Mono<AuthContext> login(UsernamePasswordAuthenticationToken authenticationToken) {
        return authenticationManager.authenticate(authenticationToken).flatMap(
                authResult ->
                        ReactiveSecurityContextHolder.getContext().map(
                                securityContext -> {
                                    securityContext.setAuthentication(authResult);
                                    return securityContext;
                                }
                        ).contextWrite(ReactiveSecurityContextHolder.withAuthentication(authResult)).map(
                                context -> buildFromSecurityContext(context)));
    }

    public Mono<LogoutResponse> logout(WebSession webSession) {
        webSession.getAttributes().remove(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        return webSession.invalidate().map(result -> new LogoutResponse());
    }
}
