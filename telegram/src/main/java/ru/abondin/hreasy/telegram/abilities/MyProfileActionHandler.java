package ru.abondin.hreasy.telegram.abilities;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import ru.abondin.hreasy.telegram.abilities.dto.MyProfileResponse;
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

    @Override
    public String commandName() {
        return "my_profile";
    }

    public void handle(BaseAbilityBot bot, HrEasyMessageContext ctx) {
        var request = webClient.get()
                .uri("/api/v1/my-profile")
                .retrieve()
                .bodyToMono(MyProfileResponse.class)
                .map(res -> {
                    var message = templateStorage.process("my_profile", cxt -> cxt.setVariable("e", res));
                    bot.silent().sendMd(message, ctx.chatId());
                    return "OK";
                });
        execHttpSync(request, bot, ctx);
    }
}
