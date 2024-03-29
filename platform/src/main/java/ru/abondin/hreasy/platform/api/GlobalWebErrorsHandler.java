package ru.abondin.hreasy.platform.api;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.r2dbc.spi.R2dbcException;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.dao.DataAccessException;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.r2dbc.BadSqlGrammarException;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.server.ServerAuthenticationEntryPoint;
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ResponseStatusException;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.config.HrEasyCorsWebFilter;

import java.util.HashMap;

/**
 * Handle errors
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
@RequiredArgsConstructor
@Slf4j
public class GlobalWebErrorsHandler implements ErrorWebExceptionHandler, ServerAccessDeniedHandler, ServerAuthenticationEntryPoint {
    private final HrEasyCorsWebFilter corsWebFilter;
    private final I18Helper i18Helper;
    private final ObjectMapper json;

    @Override
    public Mono<Void> commence(ServerWebExchange serverWebExchange, AuthenticationException e) {
        return handle(serverWebExchange, e);
    }

    @Override
    public Mono<Void> handle(ServerWebExchange serverWebExchange, AccessDeniedException e) {
        return handle(serverWebExchange, (Throwable) e);
    }

    @SneakyThrows(JsonProcessingException.class)
    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        var response = exchange.getResponse();
        final Object errorDto;
        if (ex instanceof BusinessError be) {
            response.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
            errorDto = new BusinessErrorDto(be.getCode(),
                    i18Helper.localize(be.getCode(), be.getLocalizationArgs()), be.getAttrs());
        } else if (ex instanceof BadCredentialsException be) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            errorDto = new BusinessErrorDto("errors.bad.credentials", i18Helper.localize("errors.bad.credentials"), new HashMap<>());
        } else if (ex instanceof AuthenticationException be) {
            response.setStatusCode(HttpStatus.UNAUTHORIZED);
            errorDto = new BusinessErrorDto("errors.not.authenticated", i18Helper.localize("errors.not.authenticated"), new HashMap<>());
        } else if (ex instanceof AccessDeniedException be) {
            log.warn("Access Denied error: {}", be.getLocalizedMessage());
            response.setStatusCode(HttpStatus.FORBIDDEN);
            errorDto = new BusinessErrorDto("errors.access.denied", i18Helper.localize("errors.access.denied"), new HashMap<>());
        } else if (ex instanceof ResponseStatusException be) {
            log.warn("Response status error: {} - {}", be.getStatusCode(), be.getReason());
            response.setStatusCode(be.getStatusCode());
            errorDto = new BusinessErrorDto("errors.response.not.found", i18Helper.localize("errors.response.not.found"), new HashMap<>());
        } else if (ex instanceof DataAccessException) {
            log.error("Database error", ex);
            response.setStatusCode(HttpStatus.UNPROCESSABLE_ENTITY);
            errorDto = new BusinessErrorDto("errors.data_access", i18Helper.localize("errors.data_access"), new HashMap<>());
        } else {
            log.error("Unknown error", ex);
            response.setStatusCode(HttpStatus.INTERNAL_SERVER_ERROR);
            errorDto = new AnyExceptionDto(ex);
        }
        corsWebFilter.setupCors(exchange);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        return response.writeWith(Mono.just(response.bufferFactory().wrap(json.writeValueAsBytes(errorDto))));
    }

}
