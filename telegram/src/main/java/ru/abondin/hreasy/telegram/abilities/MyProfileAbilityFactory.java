package ru.abondin.hreasy.telegram.abilities;

import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.objects.MessageContext;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.telegram.HrEasyBot;
import ru.abondin.hreasy.telegram.common.HrEasyAbilityWithAuthFactory;
import ru.abondin.hreasy.telegram.conf.I18Helper;
import ru.abondin.hreasy.telegram.conf.JwtUtil;

@Component
public class MyProfileAbilityFactory extends HrEasyAbilityWithAuthFactory {
    public static final String MY_PROFILE_COMMAND = "my_profile";

    public MyProfileAbilityFactory(I18Helper i18n, WebClient webClient, JwtUtil jwtUtil) {
        super(i18n, webClient, jwtUtil);
    }

    @Override
    public String name() {
        return MY_PROFILE_COMMAND;
    }

    @Override
    protected Mono<Void> doAction(HrEasyBot bot, MessageContext ctx) {
        return webClient
                .get()
                .uri("/api/v1/test")
                .retrieve().bodyToMono(String.class)
                .map(response -> {
                    bot.silent().send(response, ctx.chatId());
                    return null;
                });
    }
}
