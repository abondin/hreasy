package ru.abondin.hreasy.apifacade;

import lombok.RequiredArgsConstructor;
import org.springframework.cloud.gateway.route.RouteLocator;
import org.springframework.cloud.gateway.route.builder.RouteLocatorBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Central place to route all REST API to microservices
 */
@Configuration
@RequiredArgsConstructor
public class ApiFacadeConfiguration {

    private final ApiDiscoveryProps discoveryProps;

    @Bean
    public RouteLocator customRouteLocator(RouteLocatorBuilder builder) {
        //@formatter:off
        return builder.routes()
                .route(r -> r.path("/**")
                        .uri(discoveryProps.getCorems()))
                .build();
        //@formatter:on
    }
}
