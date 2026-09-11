package ru.abondin.hreasy.platform.config.external;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.authentication.ServerAuthenticationConverter;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.EmployeeBasedUserDetailsService;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.ArrayList;
import java.util.HexFormat;
import java.util.List;

/**
 * Authenticates an opaque Bearer token and loads its configured acting HR Easy user.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class ExternalTokenAuthenticationConverter implements ServerAuthenticationConverter {
    public static final String EXTERNAL_API_RESERVED_AUTHORITY = "__external_api__";

    private final ExternalApiProperties properties;
    private final EmployeeBasedUserDetailsService userDetailsService;

    @Override
    public Mono<Authentication> convert(ServerWebExchange exchange) {
        var authHeader = exchange.getRequest().getHeaders().getFirst("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Mono.empty();
        }

        var rawToken = authHeader.substring(7);
        if (!rawToken.matches("[0-9a-fA-F]{64}")) {
            return rejected("malformed token");
        }

        var token = properties.findToken(sha256(rawToken));
        if (token.isEmpty()) {
            return rejected("unknown token");
        }

        var tokenConfig = token.get();
        return userDetailsService.findForExternal(tokenConfig.getSubject())
                .map(userDetails -> authentication(userDetails.getAuthorities(), userDetails, tokenConfig.getSystem()))
                .onErrorResume(BusinessError.class, error -> rejected("inactive or unknown subject"));
    }

    private String sha256(String value) {
        try {
            var digest = MessageDigest.getInstance("SHA-256").digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest);
        } catch (NoSuchAlgorithmException exception) {
            throw new IllegalStateException("SHA-256 is not available", exception);
        }
    }

    private Authentication authentication(Iterable<? extends GrantedAuthority> userAuthorities,
                                          UserDetails userDetails,
                                          String systemId) {
        List<GrantedAuthority> authorities = new ArrayList<>();
        userAuthorities.forEach(authorities::add);
        authorities.add(new SimpleGrantedAuthority(EXTERNAL_API_RESERVED_AUTHORITY));
        var authentication = new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
        authentication.setDetails(systemId);
        return authentication;
    }

    private Mono<Authentication> rejected(String reason) {
        log.warn("Rejected external API authentication ({})", reason);
        return Mono.empty();
    }
}
