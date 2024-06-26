package ru.abondin.hreasy.platform.auth;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.AbstractUserDetailsReactiveAuthenticationManager;
import org.springframework.security.core.userdetails.UserDetails;
import reactor.core.publisher.Mono;

/**
 * Login with internal password from local database
 */
@RequiredArgsConstructor
@Slf4j
public class InternalPasswordAuthenticationProvider extends AbstractUserDetailsReactiveAuthenticationManager {
    private final EmployeeBasedUserDetailsService userDetailsService;


    @Override
    protected Mono<UserDetails> retrieveUser(String username) {
        return userDetailsService.findInternal(username);
    }
}
