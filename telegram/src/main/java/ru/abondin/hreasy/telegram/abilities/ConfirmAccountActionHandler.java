package ru.abondin.hreasy.telegram.abilities;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import ru.abondin.hreasy.telegram.common.HrEasyActionHandler;
import ru.abondin.hreasy.telegram.common.I18Helper;
import ru.abondin.hreasy.telegram.common.JwtUtil;
import ru.abondin.hreasy.telegram.common.ResponseTemplateProcessor;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

import java.time.Duration;

@Service
public class ConfirmAccountActionHandler extends HrEasyActionHandler {


    public ConfirmAccountActionHandler(I18Helper i18n, WebClient webClient, JwtUtil jwtUtil, ResponseTemplateProcessor templateStorage, HrEasyBotProps props) {
        super(i18n, webClient, jwtUtil, templateStorage, props);
    }

    public void handle(BaseAbilityBot bot, HrEasyMessageContext ctx) {
        webClient
                .post()
                .uri("/api/v1/confirm/start")
                .retrieve()
                .bodyToMono(String.class)
                .timeout(Duration.ofSeconds(10))
                .onErrorComplete()
                .doOnError(err -> defaultErrorHandling(bot, ctx, err))
                .contextWrite(jwtUtil.jwtContext(ctx.accountName()))
                .map(response -> {
                    var markdown = templateStorage.process("confirm_account", c -> {
                    });
                    bot.silent().sendMd(markdown
                            , ctx.chatId());
                    return "OK";
                }).block(props.getDefaultBotActionTimeout());
    }
}
