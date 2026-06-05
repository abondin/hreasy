package ru.abondin.hreasy.platform.config.udr;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import ru.abondin.hreasy.platform.service.dto.ValueWithStatusPropsThreshold;

@Configuration
@ConfigurationProperties(prefix = "hreasy.udr")
@Data
public class UdrProps {
    private ValueWithStatusPropsThreshold<Long> monthsWithoutReportThresholds = ValueWithStatusPropsThreshold.numberThresholds(4l, 6l);
    private ValueWithStatusPropsThreshold<Long> monthsInCompanyThresholds = ValueWithStatusPropsThreshold.numberThresholds(12l, 18l);
}
