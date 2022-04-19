package ru.abondin.hreasy.platform.service.techprofile;

import lombok.Builder;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import ru.abondin.hreasy.platform.config.HrEasyFileStorageProperties;
import ru.abondin.hreasy.platform.service.BaseAccessTokenProvider;
import ru.abondin.hreasy.platform.service.DateTimeService;

@Service
@Slf4j
public class TechProfileAccessTokenProvider extends BaseAccessTokenProvider<TechProfileAccessTokenProvider.TechProfileTokenContent> {

    public TechProfileAccessTokenProvider(HrEasyFileStorageProperties fileProps, DateTimeService timeService) {
        super(fileProps, timeService);
    }

    @Data
    @Builder
    public static class TechProfileTokenContent implements TokenContent {
        private final int techProfileEmployeeId;
    }


}
