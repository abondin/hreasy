package ru.abondin.hreasy.telegram.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

import java.util.concurrent.TimeoutException;

@Service
@Slf4j
@RequiredArgsConstructor
public class HttpRequestHelper {

    private final I18Helper i18n;
    private final HrEasyBotProps props;
    private final JwtUtil jwtUtil;

    protected void defaultErrorHandling(BaseAbilityBot bot, HrEasyActionHandler.HrEasyMessageContext ctx, Throwable ex) {
        if (ex instanceof WebClientResponseException webError) {
            if (webError.getStatusCode().equals(HttpStatus.UNAUTHORIZED)) {
                log.info("User {} unauthorized", ctx.accountName());
                bot.silent().sendMd(i18n.localize("hreasy.platform.error.unauthorized",
                        ctx.accountName(), props.getPlatform().getWebInterfaceUrl()), ctx.chatId());
            } else if (webError.getStatusCode().equals(HttpStatus.FORBIDDEN)) {
                log.info("User {} forbidden or account not confirmed", ctx.accountName());
                bot.silent().send(i18n.localize("hreasy.platform.error.forbidden", ctx.accountName()), ctx.chatId());
            } else if (webError.getStatusCode().equals(HttpStatus.UNPROCESSABLE_ENTITY)) {
                try {
                    var error = webError.getResponseBodyAs(BusinessErrorDto.class);
                    log.info("Business error: {}", error);
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
        throw new RuntimeException("Unable to execute the request to the server", ex);
    }


    /**
     * Execute HTTP request to HR Easy.
     * <ul>
     * <li>Add JWT token to request</li>
     * <li>Add default error handling</li>
     * <li>Block to synchronous code</li>
     * </ul>
     *
     * @param mono
     * @param bot
     * @param ctx
     * @return
     */
    public <T> T execHttpSync(Mono<T> mono, BaseAbilityBot bot, HrEasyActionHandler.HrEasyMessageContext ctx) {
        return mono.timeout(props.getPlatform().getHttpRequestTimeout())
                .contextWrite(jwtUtil.jwtContext(ctx.accountName()))
                .doOnError(err -> defaultErrorHandling(bot, ctx, err))
                .block(props.getDefaultBotActionTimeout());
    }
}
