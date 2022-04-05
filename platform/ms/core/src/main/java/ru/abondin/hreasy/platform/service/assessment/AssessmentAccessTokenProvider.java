package ru.abondin.hreasy.platform.service.assessment;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.config.HrEasyFileStorageProperties;
import ru.abondin.hreasy.platform.service.DateTimeService;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Assessment attachments can be downloaded via direct url without auth.
 * In that case system generates short time life access token which must be appended to the URL
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AssessmentAccessTokenProvider {

    //TODO Use JWT token instead
    private final HrEasyFileStorageProperties fileProps;
    private final DateTimeService timeService;

    @Data
    private class TokenInfo {
        private final int assessmentEmployeeId;
        private final int assessmentId;
        private final int accessorEmployeeId;
        private final OffsetDateTime expiresAt;
    }

    private final Map<String, TokenInfo> tokens = new ConcurrentHashMap<>();

    public String generateToken(int assessmentEmployeeId, int assessmentId, int accessorEmployeeId) {
        var now = timeService.now();
        var token = UUID.randomUUID().toString();
        tokens.put(token, new TokenInfo(assessmentEmployeeId, assessmentId, accessorEmployeeId, now.plus(fileProps.getAssessmentAttachmentAccessTokenTtl())));
        return token;
    }

    public Mono<Boolean> validateToken(int assessmentEmployeeId, int assessmentId, String token) {
        var tokenInfo = tokens.get(token);
        var now = timeService.now();
        if (tokenInfo == null
                || tokenInfo.getAssessmentId() != assessmentId
                || tokenInfo.getAssessmentEmployeeId() != assessmentEmployeeId
                || now.isAfter(tokenInfo.getExpiresAt())
        ) {
            return Mono.error(new BusinessError("errors.accesstoken.invalid"));
        }
        return Mono.just(true);
    }

}
