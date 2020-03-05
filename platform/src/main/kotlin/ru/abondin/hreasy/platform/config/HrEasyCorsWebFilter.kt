package ru.abondin.hreasy.platform.config


import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.stereotype.Component
import org.springframework.web.server.ServerWebExchange
import org.springframework.web.server.WebFilter
import org.springframework.web.server.WebFilterChain
import reactor.core.publisher.Mono
import ru.abondin.hreasy.platform.logger


/**
 * Cors [WebFilter]
 */
@Component
class HrEasyCorsWebFilter(val secProps: HrEasySecurityProps) : WebFilter {

    override fun filter(exchange: ServerWebExchange, chain: WebFilterChain): Mono<Void> {
        return if (setupCors(exchange)) {
            Mono.empty()
        } else chain.filter(exchange)
    }

    /**
     * Set ups cors headers in given exchange.
     *
     * @param exchange [ServerWebExchange]
     * @return true если тип запроса OPTIONS
     */
    fun setupCors(exchange: ServerWebExchange): Boolean { // CORS, allow all use our API via Ajax
        if (secProps.corsAllowedOrigins.isEmpty()) {
            logger().warn("CORS is disabled. To enable CORS set at least one origin in 'hreasy.web.sec.cors-allowed-origins' property")
            return false;
        }
        val headers = exchange.response.headers
        putOneIfEmpty(headers, "Access-Control-Allow-Origin", secProps.corsAllowedOrigins.joinToString(","))
        putOneIfEmpty(headers, "Access-Control-Max-Age", secProps.cookieMaxAge.seconds.toString())
        putOneIfEmpty(headers, "Access-Control-Allow-Credentials", "true")
        if (HttpMethod.OPTIONS == exchange.request.method) {
            putOneIfEmpty(headers, "Access-Control-Allow-Methods", "POST, GET, OPTIONS, PUT, DELETE, HEAD")
            putOneIfEmpty(headers, "Access-Control-Allow-Headers", "X-PINGOTHER, Origin, X-Requested-With, Content-Type, Accept, Date, X-Date")
            putOneIfEmpty(headers, "X-Frame-Options", "sameorigin") //SAMEORIGIN
            // if http request is options - do not process filters chain after
// because we have user authentication filter and it will fail
// with exception
            return true
        }
        return false
    }

    companion object {
        private fun putOneIfEmpty(getHeaders: HttpHeaders, header: String, headerValue: String) {
            getHeaders.putIfAbsent(header, arrayListOf(headerValue))
        }
    }
}
