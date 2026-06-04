package ru.abondin.hreasy.platform.service.assessment;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.abondin.hreasy.platform.config.HrEasyFileStorageProperties;
import ru.abondin.hreasy.platform.service.BaseAccessTokenProvider;
import ru.abondin.hreasy.platform.service.DateTimeService;

/**
 * Assessment attachments can be downloaded via direct url without auth.
 * In that case system generates short time life access token which must be appended to the URL
 */
@Service
@Slf4j
public class AssessmentAccessTokenProvider extends BaseAccessTokenProvider<AssessmentAccessTokenProvider.AssessmentAccessTokenContent> {

    public AssessmentAccessTokenProvider(HrEasyFileStorageProperties fileProps, DateTimeService timeService) {
        super(fileProps, timeService);
    }

    @Data
    @Builder
    public static class AssessmentAccessTokenContent implements TokenContent {
        private final int assessmentEmployeeId;
        private final int assessmentId;
    }

}
