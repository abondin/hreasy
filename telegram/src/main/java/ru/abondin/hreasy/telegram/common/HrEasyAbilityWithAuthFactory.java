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
import ru.abondin.hreasy.telegram.conf.I18Helper;
import ru.abondin.hreasy.telegram.conf.JwtUtil;

import java.time.Duration;
import java.util.concurrent.TimeoutException;

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
        log.error("Error on ability {}", name(), ex);
        if (ex instanceof WebClientResponseException webError) {
            if (webError.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                bot.silent().sendMd(i18n.localize("hreasy.platform.error.unauthorized", ctx.user().getUserName()), ctx.chatId());
            } else if (webError.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                bot.silent().sendMd(i18n.localize("hreasy.platform.error.forbidden", ctx.user().getUserName()), ctx.chatId());
            } else if (webError.getStatusCode().equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                try {
                    var error = webError.getResponseBodyAs(BusinessErrorDto.class);
                    bot.silent().sendMd(i18n.localize("hreasy.platform.error.business_error", error.getMessage()), ctx.chatId());
                } catch (Exception e) {
                    bot.silent().sendMd(i18n.localize("hreasy.platform.error.unprocessable_business_error"), ctx.chatId());
                }
            } else {
                bot.silent().sendMd(i18n.localize("hreasy.platform.error.server_error"), ctx.chatId());
            }
        } else if (ex instanceof TimeoutException) {
            bot.silent().sendMd(i18n.localize("hreasy.platform.error.connection_timeout"), ctx.chatId());
        } else {
            bot.silent().sendMd(i18n.localize("hreasy.platform.error.unhandled"), ctx.chatId());
        }
    }

}
