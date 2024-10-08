package ru.abondin.hreasy.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.net.URI;
import java.time.Duration;

@Configuration
@ConfigurationProperties(prefix = "hreasy.common")
@Data
public class HrEasyCommonProps {
    /**
     * Should be skill be shared on creation by default
     */
    private boolean skillAddDefaultShared = true;

    /**
     * Append email suffix for login form.
     * Example value: "@company.org"
     * <br>
     * If user populate username without email as <code>ivan.ivanov</code> system login him as
     * <code>ivan.ivanov@company.org</code>.
     */
    private String defaultEmailSuffix;

    private String defaultCalendarType = "default";
    private String defaultCalendarRegion = "RU";

    private int maxSupportRequestsInHour=5;

    /**
     * Default system account to send information via email
     */
    private String defaultEmailFrom;

    private URI webBaseUrl = URI.create("http://127.0.0.1:8080");

    private final ExcelImportProps excelImport = new ExcelImportProps();

    @Data
    public static class ExcelImportProps {
        private String dateFormat = "dd.MM.yyyy";
        private Duration importConfigTtl = Duration.ofHours(3);
    }

}
