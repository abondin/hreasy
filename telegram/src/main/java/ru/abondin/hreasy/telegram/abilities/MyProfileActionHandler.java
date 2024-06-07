package ru.abondin.hreasy.telegram.abilities;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.telegram.abilities.dto.MyProfileDto;
import ru.abondin.hreasy.telegram.common.HrEasyActionHandler;
import ru.abondin.hreasy.telegram.common.I18Helper;
import ru.abondin.hreasy.telegram.common.JwtUtil;
import ru.abondin.hreasy.telegram.common.ResponseTemplateProcessor;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

@Service
public class MyProfileActionHandler extends HrEasyActionHandler {

    public MyProfileActionHandler(I18Helper i18n, WebClient webClient, JwtUtil jwtUtil, ResponseTemplateProcessor templateStorage,
                                  HrEasyBotProps props) {
        super(i18n, webClient, jwtUtil, templateStorage, props);
    }

    public void handle(BaseAbilityBot bot, HrEasyMessageContext ctx) {
        retrieveInfoFromHrEasy(bot, ctx, "/api/v1/my-profile", MyProfileDto.class)
                .map(res -> {
                    var message = templateStorage.process("my_profile", cxt -> cxt.setVariable("e", res));
                    bot.silent().sendMd(message, ctx.chatId());
                    return "OK";
                }).block(props.getDefaultBotActionTimeout());
    }
}
