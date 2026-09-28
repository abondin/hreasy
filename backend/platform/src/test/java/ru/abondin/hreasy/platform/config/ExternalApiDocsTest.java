package ru.abondin.hreasy.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springdoc.core.configuration.SpringDocConfiguration;
import org.springdoc.core.properties.SpringDocConfigProperties;
import org.springdoc.core.properties.SwaggerUiConfigProperties;
import org.springdoc.core.properties.SwaggerUiOAuthProperties;
import org.springdoc.webflux.core.configuration.SpringDocWebFluxConfiguration;
import org.springdoc.webflux.ui.SwaggerConfig;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.ConfigDataApplicationContextInitializer;
import org.springframework.boot.test.context.runner.ReactiveWebApplicationContextRunner;
import org.springframework.boot.webflux.autoconfigure.WebFluxAutoConfiguration;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.api.GlobalWebErrorsHandler;
import ru.abondin.hreasy.platform.config.external.ExternalTokenAuthenticationConverter;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class ExternalApiDocsTest {

    @Test
    void servesExternalOnlySchemaAndSwaggerResources() {
        new ReactiveWebApplicationContextRunner()
                .withInitializer(new ConfigDataApplicationContextInitializer())
                .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class,
                        WebFluxAutoConfiguration.class, SpringDocConfiguration.class,
                        SpringDocWebFluxConfiguration.class, SwaggerConfig.class, SpringDocConfigProperties.class,
                        SwaggerUiConfigProperties.class, SwaggerUiOAuthProperties.class))
                .withUserConfiguration(TestEndpoints.class)
                .run(context -> {
                    var converter = mock(ExternalTokenAuthenticationConverter.class);
                    when(converter.convert(any())).thenReturn(Mono.empty());
                    var errors = new GlobalWebErrorsHandler(mock(HrEasyCorsWebFilter.class),
                            new I18Helper.DummyI18Helper(), new ObjectMapper());
                    var security = new WebSecurityConfig().externalApiSecurityWebFilterChain(
                            ServerHttpSecurity.http(), errors, converter);
                    var client = WebTestClient.bindToApplicationContext(context)
                            .webFilter(new WebFilterChainProxy(security)).build();
                    client.get().uri("/external/docs/openapi").exchange().expectStatus().isOk()
                            .expectCookie().doesNotExist("SESSION")
                            .expectBody().jsonPath("$.paths['/external/api/v1/test']").exists()
                            .jsonPath("$.paths['/api/v1/internal']").doesNotExist();
                    client.get().uri("/external/docs/openapi.yaml").exchange().expectStatus().isOk();
                    client.get().uri("/external/docs/openapi/swagger-config").exchange().expectStatus().isOk()
                            .expectBody().jsonPath("$.url").isEqualTo("/external/docs/openapi");
                    var redirect = client.get().uri("/external/docs/swagger-ui.html").exchange()
                            .expectStatus().is3xxRedirection().returnResult(Void.class)
                            .getResponseHeaders().getLocation();
                    assertTrue(redirect.getPath().startsWith("/external/docs/"));
                    client.get().uri(redirect).exchange().expectStatus().isOk();
                    client.get().uri(redirect.resolve("swagger-ui-bundle.js"))
                            .exchange().expectStatus().isOk();
                    client.get().uri(redirect.resolve("swagger-initializer.js"))
                            .exchange().expectStatus().isOk().expectBody(String.class)
                            .value(script -> assertTrue(script.contains("/external/docs/openapi/swagger-config")));
                    client.get().uri("/external/api/v1/test").exchange().expectStatus().isUnauthorized()
                            .expectCookie().doesNotExist("SESSION");
                });
    }

    @RestController
    static class TestEndpoints {
        @GetMapping("/external/api/v1/test")
        String external() {
            return "external";
        }

        @GetMapping("/api/v1/internal")
        String internal() {
            return "internal";
        }
    }
}
