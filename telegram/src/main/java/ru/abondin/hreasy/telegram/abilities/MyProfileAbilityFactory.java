package ru.abondin.hreasy.telegram.abilities;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.objects.MessageContext;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.telegram.HrEasyBot;
import ru.abondin.hreasy.telegram.abilities.dto.MyProfileDto;
import ru.abondin.hreasy.telegram.common.HrEasyAbilityWithAuthFactory;
import ru.abondin.hreasy.telegram.common.I18Helper;
import ru.abondin.hreasy.telegram.common.JwtUtil;
import ru.abondin.hreasy.telegram.common.ResponseTemplateProcessor;

@Component
public class MyProfileAbilityFactory extends HrEasyAbilityWithAuthFactory {
    public static final String MY_PROFILE_COMMAND = "my_profile";

    public MyProfileAbilityFactory(I18Helper i18n, WebClient webClient, JwtUtil jwtUtil, ResponseTemplateProcessor templateStorage) {
        super(i18n, webClient, jwtUtil, templateStorage);
    }


    @Override
    public String name() {
        return MY_PROFILE_COMMAND;
    }

    @Override
    protected Mono<String> doAction(HrEasyBot bot, MessageContext ctx) {
        return retrieveInfoFromHrEasy(bot, ctx, "/api/v1/my-profile", MyProfileDto.class,
                response -> {
                    var markdown = templateStorage.process(name(), c -> {
                        c.setVariable("e", response);
                    });
                    bot.silent().sendMd(markdown
                            , ctx.chatId());
                    return "OK";
                });
    }
}
