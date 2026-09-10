package ru.abondin.hreasy.platform.config;

import org.junit.jupiter.api.Test;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;
import org.springframework.mock.web.server.MockServerWebExchange;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.web.server.context.WebSessionServerSecurityContextRepository;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.config.telegram.TelegramJwtAuthenticationConverter;
import ru.abondin.hreasy.platform.tg.TgAuthLogService;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

class WebSecurityConfigTest {

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
