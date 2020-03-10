package ru.abondin.hreasy.platform.api

import com.fasterxml.jackson.databind.ObjectMapper
import org.apache.commons.lang3.exception.ExceptionUtils
import org.springframework.boot.web.reactive.error.ErrorWebExceptionHandler
import org.springframework.context.MessageSource
import org.springframework.core.Ordered
import org.springframework.core.annotation.Order
import org.springframework.http.HttpStatus
import org.springframework.http.MediaType
import org.springframework.security.access.AccessDeniedException
import org.springframework.security.authentication.BadCredentialsException
import org.springframework.security.core.AuthenticationException
import org.springframework.security.web.server.ServerAuthenticationEntryPoint
import org.springframework.security.web.server.authorization.ServerAccessDeniedHandler
import org.springframework.stereotype.Component
import org.springframework.web.server.ResponseStatusException
import org.springframework.web.server.ServerWebExchange
import reactor.core.publisher.Mono
import ru.abondin.hreasy.platform.BusinessError
import ru.abondin.hreasy.platform.config.HrEasyCorsWebFilter
import ru.abondin.hreasy.platform.logger
import java.util.*

/**
 * Handle errors
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
class GlobalWebErrorsHandler(
        val corsWebFilter: HrEasyCorsWebFilter,
        val messageSource: MessageSource,
        val json: ObjectMapper
) : ErrorWebExceptionHandler, ServerAccessDeniedHandler, ServerAuthenticationEntryPoint {

    override fun handle(exchange: ServerWebExchange, ex: Throwable): Mono<Void> {
        logger().error("Handle error", ex);
        val response = exchange.response;
        val errorDto: Any;
        when (ex) {
            is BusinessError -> {
                response.statusCode = HttpStatus.UNPROCESSABLE_ENTITY;
                errorDto = BusinessErrorDto(ex.code, localize(ex.code, ex.localizationArgs), ex.attrs);
            }
            is BadCredentialsException -> {
                response.statusCode = HttpStatus.UNAUTHORIZED;
                errorDto = BusinessErrorDto("errors.bad.credentials", localize("errors.bad.credentials"), mapOf());
            }
            is AuthenticationException -> {
                response.statusCode = HttpStatus.UNAUTHORIZED;
                errorDto = BusinessErrorDto("errors.not.authenticated", localize("errors.not.authenticated"), mapOf());
            }
            is AccessDeniedException -> {
                response.statusCode = HttpStatus.FORBIDDEN;
                errorDto = BusinessErrorDto("errors.access.denied", localize("errors.access.denied"), mapOf());
            }
            is ResponseStatusException ->{
                response.statusCode = HttpStatus.NOT_FOUND;
                errorDto = BusinessErrorDto("errors.response.not.found", localize("errors.response.not.found"), mapOf());
            }
            else -> {
                response.statusCode = HttpStatus.INTERNAL_SERVER_ERROR;
                errorDto = AnyExceptionDto(ex);
            }
        }
        corsWebFilter.setupCors(exchange);
        response.headers.contentType = MediaType.APPLICATION_JSON;
        return response.writeWith(Mono.just(response.bufferFactory().wrap(json.writeValueAsBytes(errorDto))));
    }

    override fun handle(exchange: ServerWebExchange, denied: AccessDeniedException): Mono<Void> {
        return handle(exchange, denied as Throwable);
    }

    override fun commence(exchange: ServerWebExchange, e: AuthenticationException): Mono<Void> {
        return handle(exchange, e as Throwable);
    }


    private fun localize(code: String, vararg args: Any): String {
        return messageSource.getMessage(code, args, Locale("ru"));
    }
}

data class AnyExceptionDto(val message: String) {
    constructor(ex: Throwable) : this(ExceptionUtils.getMessage(ex));
}

data class BusinessErrorDto(
        val code: String,
        val message: String,
        val attrs: Map<String, String>
) {
}