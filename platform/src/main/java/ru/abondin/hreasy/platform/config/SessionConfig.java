package ru.abondin.hreasy.platform.config;

import lombok.Data;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.session.MapSession;
import org.springframework.session.ReactiveMapSessionRepository;
import org.springframework.session.ReactiveSessionRepository;
import org.springframework.session.config.annotation.web.server.EnableSpringWebSession;
import org.springframework.web.server.session.CookieWebSessionIdResolver;
import org.springframework.web.server.session.WebSessionIdResolver;

import java.util.concurrent.ConcurrentHashMap;

@Configuration
@EnableSpringWebSession
@Data
public class SessionConfig {

    @Bean
    ReactiveSessionRepository<MapSession> reactiveSessionRepository(HrEasySecurityProps sessionProps) {
        var sessionRepository = new ReactiveMapSessionRepository(new ConcurrentHashMap<>());
        sessionRepository.setDefaultMaxInactiveInterval(
                sessionProps.getMaxInactiveInterval());
        return sessionRepository;
    }


    @Bean
    WebSessionIdResolver webSessionIdResolver(HrEasySecurityProps sessionProps) {
        var resolver = new CookieWebSessionIdResolver();
        resolver.setCookieName("X-Auth-Token");
        resolver.setCookieMaxAge(sessionProps.getCookieMaxAge());
        return resolver;
    }
}
