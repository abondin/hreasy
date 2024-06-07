package ru.abondin.hreasy.telegram.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

/**
 * Check HR Easy JWT token in bot database.
 * Requests new if not found
 */
@RequiredArgsConstructor
@Slf4j
public abstract class HrEasyActionHandler {
    protected final I18Helper i18n;
    protected final WebClient webClient;
    protected final JwtUtil jwtUtil;
    protected final ResponseTemplateProcessor templateStorage;
    protected final HrEasyBotProps props;

    public record HrEasyMessageContext(String accountName, long chatId) {
    }

    protected void defaultErrorHandling(BaseAbilityBot bot, HrEasyMessageContext ctx, Throwable ex) {
        if (ex instanceof WebClientResponseException webError) {
            if (webError.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                log.debug("User {} unauthorized", ctx.accountName());
                bot.silent().send(i18n.localize("hreasy.platform.error.unauthorized",
                        ctx.accountName()), ctx.chatId());
            } else if (webError.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                log.debug("User {} forbidden or account not confirmed", ctx.accountName());
                bot.silent().send(i18n.localize("hreasy.platform.error.forbidden", ctx.accountName()), ctx.chatId());
            } else if (webError.getStatusCode().equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                try {
                    var error = webError.getResponseBodyAs(BusinessErrorDto.class);
                    log.debug("Business error: {}", error);
                    bot.silent().send(i18n.localize("hreasy.platform.error.business_error", error.getMessage()), ctx.chatId());
                } catch (Exception e) {
                    log.error("Unprocessable business error", e);
                    bot.silent().send(i18n.localize("hreasy.platform.error.unprocessable_business_error"), ctx.chatId());
                }
            } else {
                log.error("Unhandled client error", ex);
                bot.silent().send(i18n.localize("hreasy.platform.error.server_error"), ctx.chatId());
            }
        } else if (ex instanceof TimeoutException) {
            log.error("Connection to HR Easy timeout");
            bot.silent().send(i18n.localize("hreasy.platform.error.connection_timeout"), ctx.chatId());
        } else {
            log.error("Unhandled error", ex);
            bot.silent().send(i18n.localize("hreasy.platform.error.unhandled"), ctx.chatId());
        }
    }

    protected <T> Mono<T> retrieveInfoFromHrEasy(BaseAbilityBot bot, HrEasyMessageContext ctx, String uri, Class<T> resultType) {
        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(resultType)
                .timeout(Duration.ofSeconds(10))
                .contextWrite(jwtUtil.jwtContext(ctx.accountName()))
                .doOnError(err -> defaultErrorHandling(bot, ctx, err))
                .onErrorComplete();
    }

}
