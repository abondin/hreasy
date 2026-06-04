package ru.abondin.hreasy.notifyms.config;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

@Configuration
public class SecurityConfig {

    @Bean
    SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity http) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .authorizeExchange(exchanges -> exchanges.anyExchange().permitAll())
                .build();
    }

    @Component
    @RequiredArgsConstructor
    public static class InternalTokenFilter implements WebFilter, InitializingBean {
        private final NotificationProperties props;

        @Override
        public void afterPropertiesSet() {
            if (!StringUtils.hasText(props.getHttpToken())) {
                throw new IllegalStateException("hreasy.notifications.http-token must be configured");
            }
        }

        @Override
        public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
            var path = exchange.getRequest().getPath().pathWithinApplication().value();
            if (!path.startsWith("/api/")) {
                return chain.filter(exchange);
            }
            var configuredToken = props.getHttpToken();
            var auth = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
            if (("Bearer " + configuredToken).equals(auth)) {
                return chain.filter(exchange);
            }
            exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
            return exchange.getResponse().setComplete();
        }
    }
}
