package ru.abondin.hreasy.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "hreasy.common")
@Data
public class HrEasyCommonProps {
    /**
     * Should be skill be shared on creation by default
     */
   private boolean skillAddDefaultShared = true;

    /**
     * Append email suffix to login form.
     * Example value: "@company.org"
     * <br>
     * If user populate username without email as <code>ivan.ivanov</code> system login him as
     * <code>ivan.ivanov@company.org</code>.
     */
   private String defaultEmailSuffix;

   private String defaultCalendarType="default";
   private String defaultCalendarRegion="RU";

    /**
     * Default system account to send information via email
     */
    private String defaultEmailFrom;
}
