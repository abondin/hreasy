package ru.abondin.hreasy.platform.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextImpl;
import org.springframework.security.web.server.WebFilterChainProxy;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import org.springframework.test.web.reactive.server.WebTestClient;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.api.GlobalWebErrorsHandler;
import ru.abondin.hreasy.platform.config.external.ExternalTokenAuthenticationConverter;
import ru.abondin.hreasy.platform.config.telegram.TelegramJwtAuthenticationConverter;
import ru.abondin.hreasy.platform.tg.TgAuthLogService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.web.reactive.function.server.RequestPredicates.GET;
import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RouterFunctions.route;
import static org.springframework.web.reactive.function.server.ServerResponse.ok;

class WebSecurityConfigTest {

    @Test
    void externalApiChainIsBearerOnlyStatelessAndReadOnly() {
        var converter = mock(ExternalTokenAuthenticationConverter.class);
        var externalAuthority = new SimpleGrantedAuthority(
                ExternalTokenAuthenticationConverter.EXTERNAL_API_RESERVED_AUTHORITY);
        var externalAuth = UsernamePasswordAuthenticationToken.authenticated(
                "external", null, List.of(externalAuthority));
        when(converter.convert(any())).thenAnswer(invocation -> {
            var exchange = invocation.<ServerWebExchange>getArgument(0);
            return "Bearer valid".equals(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                    ? Mono.just(externalAuth)
                    : Mono.empty();
        });
        var errorHandler = new GlobalWebErrorsHandler(
                mock(HrEasyCorsWebFilter.class), new I18Helper.DummyI18Helper(), new ObjectMapper());
        var securityChain = new WebSecurityConfig().externalApiSecurityWebFilterChain(
                ServerHttpSecurity.http(), errorHandler, converter);
        var webSessionAuth = UsernamePasswordAuthenticationToken.authenticated("web", null, List.of());
        WebFilter sessionSeeder = (exchange, chain) -> exchange.getSession()
                .flatMap(session -> {
                    session.getAttributes().put(
                            WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME,
                            new SecurityContextImpl(webSessionAuth));
                    return chain.filter(exchange);
                });
        var routes = route(GET("/external/test"), _ -> ok().bodyValue("ok"))
                .andRoute(GET("/external/docs/openapi"), _ -> ok().bodyValue("docs"))
                .andRoute(POST("/external/docs/openapi"), _ -> ok().bodyValue("docs"))
                .andRoute(POST("/external/test"), _ -> ok().bodyValue("ok"));
        var client = WebTestClient.bindToRouterFunction(routes)
                .webFilter(sessionSeeder, new WebFilterChainProxy(securityChain))
                .build();

        client.get().uri("/external/docs/openapi").exchange().expectStatus().isOk();
        client.post().uri("/external/docs/openapi").exchange().expectStatus().isForbidden();
        client.get().uri("/external/test").exchange().expectStatus().isUnauthorized();
        client.get().uri("/external/test").header(HttpHeaders.AUTHORIZATION, "Bearer invalid")
                .exchange().expectStatus().isUnauthorized();
        client.post().uri("/external/test").header(HttpHeaders.AUTHORIZATION, "Bearer valid")
                .exchange().expectStatus().isForbidden();
        client.get().uri("/external/test").header(HttpHeaders.AUTHORIZATION, "Bearer valid")
                .exchange().expectStatus().isOk();
    }

    @Test
    void telegramPostRequiresBearerAndDoesNotRequireCsrfToken() {
        var converter = mock(TelegramJwtAuthenticationConverter.class);
        var authLogService = mock(TgAuthLogService.class);
        var auth = UsernamePasswordAuthenticationToken.authenticated("telegram", null, List.of(
                new SimpleGrantedAuthority(TelegramJwtAuthenticationConverter.TELEGRAM_CONFIRMED_RESERVED_AUTHORITY)));
        when(converter.convert(any())).thenAnswer(invocation -> {
            var exchange = invocation.<ServerWebExchange>getArgument(0);
            return "Bearer valid".equals(exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION))
                    ? Mono.just(auth) : Mono.empty();
        });
        when(authLogService.log(auth)).thenReturn(Mono.empty());
        var errorHandler = new GlobalWebErrorsHandler(
                mock(HrEasyCorsWebFilter.class), new I18Helper.DummyI18Helper(), new ObjectMapper());
        var securityChain = new WebSecurityConfig().internalApiSecurityWebFilterChain(
                ServerHttpSecurity.http(), errorHandler, converter, authLogService);
        var client = WebTestClient.bindToRouterFunction(
                        route(POST("/telegram/api/v1/support/request"), _ -> ok().bodyValue("ok")))
                .webFilter(new WebFilterChainProxy(securityChain))
                .build();

        client.post().uri("/telegram/api/v1/support/request").exchange().expectStatus().isUnauthorized();
        client.post().uri("/telegram/api/v1/support/request").header(HttpHeaders.AUTHORIZATION, "Bearer valid")
                .exchange().expectStatus().isOk().expectCookie().doesNotExist("SESSION");
    }

    @Test
    void telegramAuthenticationDoesNotCreateWebSession() {
        var converter = mock(TelegramJwtAuthenticationConverter.class);
        var authLogService = mock(TgAuthLogService.class);
        var authentication = UsernamePasswordAuthenticationToken.authenticated("user", "token", List.of());
        var exchange = MockServerWebExchange.from(MockServerHttpRequest.get("/telegram/api/v1/employees"));
        when(converter.convert(exchange)).thenReturn(Mono.just(authentication));
        when(authLogService.log(authentication)).thenReturn(Mono.empty());

        var filter = new WebSecurityConfig().telegramAuthenticationWebFilter(converter, authLogService);

        StepVerifier.create(filter.filter(exchange, ignored -> Mono.empty())).verifyComplete();
        assertFalse(exchange.getSession().block().getAttributes().containsKey(
                WebSessionServerSecurityContextRepository.DEFAULT_SPRING_SECURITY_CONTEXT_ATTR_NAME));
    }
}
