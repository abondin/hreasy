package ru.abondin.hreasy.platform.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration
import java.time.Duration

@Configuration
@ConfigurationProperties(prefix = "hreasy.web.sec")
class HrEasySecurityProps {
    /**
     * Web session max inactive interval
     */
    var maxInactiveInterval: Duration = Duration.ofDays(7);
    /**
     * Cookie max age
     */
    var cookieMaxAge: Duration = Duration.ofHours(48);

    /**
     * Allowed origin for cors
     */
    var corsAllowedOrigins: Array<String> = arrayOf();

    /**
     * Security User maps to database employee entity using email.
     * So, we have to map security username to employee email
     */
    var emailSuffix = "@stm-labs.ru";


    /**
     * For development purposes only.
     * Auth every user with the secret master password
     */
    var masterPassword = "";
}