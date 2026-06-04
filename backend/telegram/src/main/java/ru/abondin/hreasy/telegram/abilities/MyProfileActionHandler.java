package ru.abondin.hreasy.telegram.abilities;

import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import ru.abondin.hreasy.telegram.abilities.dto.TgMyProfileResponse;
import ru.abondin.hreasy.telegram.common.*;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

@Service
public class MyProfileActionHandler extends HrEasyActionHandler {

    public static final String COMMAND_NAME = "my_profile";

    public MyProfileActionHandler(I18Helper i18n, WebClient webClient, HttpRequestHelper httpHelper, ResponseTemplateProcessor templateStorage,
                                  HrEasyBotProps props) {
        super(i18n, webClient, httpHelper, templateStorage, props);
    }

    @Override
    public String commandName() {
        return COMMAND_NAME;
    }

    public void handle(BaseAbilityBot bot, HrEasyMessageContext ctx) {
        var request = webClient.get()
                .uri("/api/v1/my-profile")
                .retrieve()
                .bodyToMono(TgMyProfileResponse.class)
                .map(res -> {
                    var message = templateStorage.process("my_profile", cxt -> cxt.setVariable("e", res));
                    bot.silent().send(message, ctx.chatId());
                    return "OK";
                });

        httpHelper.execHttpSync(request, bot, ctx);
    }
}
