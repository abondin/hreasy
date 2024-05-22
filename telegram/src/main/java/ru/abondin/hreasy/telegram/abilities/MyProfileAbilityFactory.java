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

import java.time.Duration;

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
        return webClient
                .get()
                .uri("/api/v1/my-profile")
                .retrieve()
                .bodyToMono(MyProfileDto.class)
                .timeout(Duration.ofSeconds(10))
                .doOnError(err -> defaultErrorHandling(bot, ctx, err))
                .contextWrite(jwtUtil.jwtContext(ctx))
                .map(response -> {
                    var markdown = templateStorage.process(name(), c -> {
                        c.setVariable("e", response);
                    });
                    bot.silent().sendMd(markdown
                            , ctx.chatId());
                    return "OK";
                });
    }
}
