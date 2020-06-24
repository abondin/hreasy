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
    private String serverUrl = "ldap://dc.stm.local";
    /**
     * User DN to authorize
     */
    private String userDn = "CN=<admin_user>,CN=Users,DC=stm,DC=local";
    private String userPassword = "<PASSWORD>";
    private String searchBase = "OU=Internal,OU=Enterprise,DC=stm,DC=local";
    private String searchFilter = "sAMAccountName={0}";
}
