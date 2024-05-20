package ru.abondin.hreasy.telegram.conf;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Component
public class JwtUtil {

    private final SecretKey secretKey;
    private final Duration tokenExperiation;

    public JwtUtil(HrEasyBotProps props) {
        this.secretKey = Keys.hmacShaKeyFor(props.getSecurity().getJwtTokenSecret().getBytes(StandardCharsets.UTF_8));
        this.tokenExperiation = props.getSecurity().getJwtTokenExpiration();
    }

    public String generateToken(String username) {
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