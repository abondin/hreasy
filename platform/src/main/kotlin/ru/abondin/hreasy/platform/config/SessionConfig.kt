package ru.abondin.hreasy.platform.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.session.MapSession
import org.springframework.session.ReactiveMapSessionRepository
import org.springframework.session.ReactiveSessionRepository
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession
import org.springframework.web.server.session.CookieWebSessionIdResolver
import org.springframework.web.server.session.WebSessionIdResolver
import java.util.concurrent.ConcurrentHashMap


@Configuration
@EnableSpringWebSession
class SessionConfig {

    @Bean
    fun reactiveSessionRepository(sessionProps: HrEasySecurityProps): ReactiveSessionRepository<MapSession> {
        val sessionRepository = ReactiveMapSessionRepository(ConcurrentHashMap());
        sessionRepository.setDefaultMaxInactiveInterval(sessionProps.maxInactiveInterval.seconds.toInt());
        return sessionRepository;
    }


    @Bean
    fun webSessionIdResolver(sessionProps: HrEasySecurityProps): WebSessionIdResolver {
        val resolver = CookieWebSessionIdResolver();
        resolver.cookieName = "X-Auth-Token";
        resolver.cookieMaxAge = sessionProps.cookieMaxAge;
        return resolver;
    }
}


