package ru.abondin.hreasy.platform.ldap;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTest;
import org.springframework.context.annotation.Import;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.context.ActiveProfiles;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.config.LdapConfigurationProperties;
import ru.abondin.hreasy.platform.config.WebSecurityConfig;

// TODO Does not work with Embedded Ldap Server for some reason
@Disabled
@Import({WebSecurityConfig.class, LdapConfigurationProperties.class})
@ActiveProfiles("ldaptest")
@DataLdapTest
public class LdapConnectionTest {

    @Autowired
    private ReactiveAuthenticationManager authManager;


    @Test
    public void testLdap() {
        StepVerifier.create(
                authManager.authenticate(new UsernamePasswordAuthenticationToken("bob", "bobspassword"))
        ).expectNextMatches(auth -> ((UserDetails) auth.getPrincipal()).getUsername().equals("bob")).verifyComplete();
    }
}
