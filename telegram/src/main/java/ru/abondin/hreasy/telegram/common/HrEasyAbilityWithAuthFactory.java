package ru.abondin.hreasy.telegram.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.telegram.HrEasyBot;

import java.time.Duration;
import java.util.concurrent.TimeoutException;
import java.util.function.Function;

/**
 * Check HR Easy JWT token in bot database.
 * Requests new if not found
 */
@RequiredArgsConstructor
@Slf4j
public abstract class HrEasyAbilityWithAuthFactory implements HrEasyAbilityFactory {
    protected final I18Helper i18n;
    protected final WebClient webClient;
    protected final JwtUtil jwtUtil;
    protected final ResponseTemplateProcessor templateStorage;

    public Ability create(HrEasyBot bot) {
        return Ability.builder()
                .name(name())
                .info(name())
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .enableStats()
                .action(ctx -> prepareTokenAndDoAction(bot, ctx))
                .build();
    }

    private void prepareTokenAndDoAction(HrEasyBot bot, MessageContext ctx) {
        log.info("Performing {} by {} in chat", name(), ctx.user(), ctx.chatId());
        doAction(bot, ctx).block(Duration.ofSeconds(20));
    }

    protected abstract <T> Mono<T> doAction(HrEasyBot bot, MessageContext ctx);

    protected void defaultErrorHandling(HrEasyBot bot, MessageContext ctx, Throwable ex) {
        if (ex instanceof WebClientResponseException webError) {
            if (webError.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                log.debug("User {} unauthorized", ctx.user().getUserName());
                bot.silent().send(i18n.localize("hreasy.platform.error.unauthorized",
                        ctx.user().getUserName()), ctx.chatId());
            } else if (webError.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                log.debug("User {} forbidden or account not confirmed", ctx.user().getUserName());
                bot.silent().send(i18n.localize("hreasy.platform.error.forbidden", ctx.user().getUserName()), ctx.chatId());
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

    protected <T> Mono<String> retrieveInfoFromHrEasy(HrEasyBot bot, MessageContext ctx, String uri, Class<T> resultType, Function<T, String> handler) {
        return webClient
                .get()
                .uri(uri)
                .retrieve()
                .bodyToMono(resultType)
                .timeout(Duration.ofSeconds(10))
                .contextWrite(jwtUtil.jwtContext(ctx))
                .doOnError(err -> defaultErrorHandling(bot, ctx, err))
                .onErrorComplete()
                .map(handler);
    }

}
