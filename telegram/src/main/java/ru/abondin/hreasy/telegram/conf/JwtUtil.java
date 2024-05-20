package ru.abondin.hreasy.telegram.conf;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.objects.MessageContext;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

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
    private final Duration tokenExperiation;

    public JwtUtil(HrEasyBotProps props) {
        this.secretKey = Keys.hmacShaKeyFor(props.getPlatform().getJwtTokenSecret().getBytes(StandardCharsets.UTF_8));
        this.tokenExperiation = props.getPlatform().getJwtTokenExpiration();
    }

    public Mono<Context> putJwtTokenToContext(MessageContext ctx) {
        return Mono.deferContextual(Mono::just)
                .map(c -> Context.of(CONTEXT_USER_KEY, generateToken(ctx.user().getUserName())));
    }

    public Mono<String> getJwtTokenFromContext(){
        return Mono.deferContextual(Mono::just)
                .flatMap(ctx -> Mono.justOrEmpty(ctx.getOrEmpty(CONTEXT_USER_KEY)));
    }



    private String generateToken(String username) {
        Map<String, Object> claims = new HashMap<>();
        return createToken(claims, username);
    }

    private String createToken(Map<String, Object> claims, String subject) {
        var now = OffsetDateTime.now();
        var expiredAt = now.plus(tokenExperiation);
        return Jwts
                .builder()
                .claims(claims)
                .subject(subject)
                .issuedAt(Date.from(now.toInstant()))
                .expiration(Date.from(expiredAt.toInstant()))
                .signWith(secretKey).compact();

    }
}