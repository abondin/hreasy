package ru.abondin.hreasy.platform.ldap

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.autoconfigure.data.ldap.DataLdapTest
import org.springframework.context.annotation.Import
import org.springframework.security.authentication.ReactiveAuthenticationManager
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken
import org.springframework.security.core.userdetails.UserDetails
import org.springframework.test.context.ActiveProfiles
import reactor.test.StepVerifier
import ru.abondin.hreasy.platform.config.LdapConfigurationProperties
import ru.abondin.hreasy.platform.config.WebSecurityConfig
import java.util.function.Predicate


// TODO Does not work with Embedded Ldap Server for some reason
@Disabled
@Import(WebSecurityConfig::class, LdapConfigurationProperties::class)
@ActiveProfiles("ldaptest")
@DataLdapTest
class LdapConnectionTest {

    @Autowired
    lateinit var authManager: ReactiveAuthenticationManager;


    @Test
    fun testLdap() {
        StepVerifier.create(
                authManager.authenticate(UsernamePasswordAuthenticationToken("bob", "bobspassword"))
        ).expectNextMatches(Predicate { auth ->
            return@Predicate (auth.principal as UserDetails).username.equals("bob");
        }).verifyComplete();

    }
}