package ru.abondin.hreasy.platform.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * Properties to configure LDAP
 */
@Configuration
@ConfigurationProperties("hreasy.ldap")
@Data
public class LdapConfigurationProperties {
    /**
     * Server URL
     */
    private String serverUrl;
    /**
     * User DN to authorize
     */
    private String userDn;
    private String userPassword = "<PASSWORD>";
    private String searchBase;
    private String searchFilter = "userPrincipalName={0}";
}
