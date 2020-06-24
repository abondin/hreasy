package ru.abondin.hreasy.platform.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.util.Arrays;

/**
 * Cors [WebFilter]
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class HrEasyCorsWebFilter implements WebFilter {

    private final HrEasySecurityProps secProps;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (setupCors(exchange)) {
            return Mono.empty();
        } else {
            return chain.filter(exchange);
        }
    }

    /**
     * Set ups cors headers in given exchange.
     *
     * @param exchange [ServerWebExchange]
     * @return true если тип запроса OPTIONS
     */
    public boolean setupCors(ServerWebExchange exchange) { // CORS, allow all use our API via Ajax
        if (secProps.getCorsAllowedOrigins().isEmpty()) {
            log.debug("CORS is disabled. To enable CORS set at least one origin in 'hreasy.web.sec.cors-allowed-origins' property");
            return false;
        }
        var headers = exchange.getResponse().getHeaders();
        putOneIfEmpty(headers, "Access-Control-Allow-Origin", Strings.join(secProps.getCorsAllowedOrigins(), ','));
        putOneIfEmpty(headers, "Access-Control-Max-Age", Long.toString(secProps.getCookieMaxAge().getSeconds()));
        putOneIfEmpty(headers, "Access-Control-Allow-Credentials", "true");
        if (HttpMethod.OPTIONS == exchange.getRequest().getMethod()) {
            putOneIfEmpty(headers, "Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD");
            putOneIfEmpty(headers, "Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept, Date, X-Date");
            putOneIfEmpty(headers, "X-Frame-Options", "sameorigin");
            //SAMEORIGIN
            // if http request is options - do not process filters chain after
            // because we have user authentication filter and it will fail
            // with exception
            return true;
        }
        return false;
    }

    private void putOneIfEmpty(HttpHeaders getHeaders, String header, String headerValue) {
        getHeaders.putIfAbsent(header, Arrays.asList(headerValue));
    }
}
