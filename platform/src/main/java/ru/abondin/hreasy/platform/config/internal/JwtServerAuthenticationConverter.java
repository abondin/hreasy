package ru.abondin.hreasy.platform.config.internal;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.auth.EmployeeBasedUserDetailsService;

/**
 * For service-to-service request authentication only
 * Populates Authentication from JWT token from Authorization header
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class JwtServerAuthenticationConverter implements ServerAuthenticationConverter {

    private final JwtUtil jwtUtil;

    private final EmployeeBasedUserDetailsService userDetailsService;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String jwt = authHeader.substring(7);
            String username;
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                log.error("Invalid JWT signature", e);
                return Mono.empty();
            }
            return userDetailsService.findByUsername(username, AuthContext.LoginType.INTERNAL_SERVICE)
                    .map(userDetails -> {
                        if (jwtUtil.validateToken(jwt, userDetails.getUsername())) {
                            return new UsernamePasswordAuthenticationToken(userDetails, null, userDetails.getAuthorities());
                        }
                        return null;
                    });
        }
        return Mono.empty();
    }
}