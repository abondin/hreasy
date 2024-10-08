package ru.abondin.hreasy.telegram.common;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.objects.MessageContext;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private static final String CONTEXT_USER_KEY = "context-user-jwt-token";

    private final SecretKey secretKey;
    private final Duration tokenExperation;

    public JwtUtil(HrEasyBotProps props) {
        this.secretKey = Keys.hmacShaKeyFor(props.getPlatform().getJwtTokenSecret().getBytes(StandardCharsets.UTF_8));
        this.tokenExperation = props.getPlatform().getJwtTokenExpiration();
    }

    public Context jwtContext(String accountName) {
        return Context.of(CONTEXT_USER_KEY, generateToken(accountName));
    }

    public Mono<String> getJwtTokenFromContext() {
        return Mono.deferContextual(ctx -> Mono.justOrEmpty(ctx.getOrEmpty(CONTEXT_USER_KEY)));
    }


    private String generateToken(String accountName) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, accountName);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        var now = OffsetDateTime.now();
        var expiredAt = now.plus(tokenExperation);
        return Jwts
                .builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(expiredAt.toInstant()))
                .signWith(secretKey).compact();

    }
}