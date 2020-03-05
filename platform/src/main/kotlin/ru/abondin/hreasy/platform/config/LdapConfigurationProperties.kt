package ru.abondin.hreasy.platform.config

import org.springframework.boot.context.properties.ConfigurationProperties
import org.springframework.context.annotation.Configuration

/**
 * Properties to configure LDAP
 */
@Configuration
@ConfigurationProperties("hreasy.ldap")
class LdapConfigurationProperties {
    /**
     * Server URL
     */
    var serverUrl = "ldap://dc.stm.local";
    /**
     * User DN to authorize
     */
    var userDn = "CN=<admin_user>,CN=Users,DC=stm,DC=local";
    var userPassword = "<PASSWORD>";
    var searchBase = "OU=Internal,OU=Enterprise,DC=stm,DC=local";
    var searchFilter = "sAMAccountName={0}";
}