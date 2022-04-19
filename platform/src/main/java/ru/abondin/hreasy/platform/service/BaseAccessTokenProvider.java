package ru.abondin.hreasy.platform.service;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.config.HrEasyFileStorageProperties;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Personal documents can be downloaded via direct url without auth.
 * In that case system generates short time life access token which must be appended to the URL
 */
@RequiredArgsConstructor
@Slf4j
public abstract class BaseAccessTokenProvider
        <T extends BaseAccessTokenProvider.TokenContent> {

    public interface TokenContent {
    }

    @Data
    @RequiredArgsConstructor
    public static class TokenInfo<T extends BaseAccessTokenProvider.TokenContent> {
        private final T content;
        private final OffsetDateTime expiresAt;
    }

    //TODO Use JWT token instead
    private final HrEasyFileStorageProperties fileProps;
    private final DateTimeService timeService;


    private final Map<String, TokenInfo<T>> tokens = new ConcurrentHashMap<>();

    public String generateToken(T content, int requesterEmployeeId) {
        log.debug("Generate token {} for employee {}", content, requesterEmployeeId);
        var now = timeService.now();
        var token = UUID.randomUUID().toString();
        tokens.put(token, new TokenInfo(content, now.plus(fileProps.getDefaultDownloadAccessTokenTtl())));
        return token;
    }

    public Mono<Boolean> validateToken(T content, String token) {
        var tokenInfo = tokens.get(token);
        var now = timeService.now();
        if (tokenInfo == null
                || !tokenInfo.getContent().equals(content)
                || now.isAfter(tokenInfo.getExpiresAt())
        ) {
            return Mono.error(new BusinessError("errors.accesstoken.invalid"));
        }
        return Mono.just(true);
    }

}
