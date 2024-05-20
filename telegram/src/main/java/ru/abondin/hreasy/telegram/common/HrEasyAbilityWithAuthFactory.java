package ru.abondin.hreasy.telegram.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.telegram.HrEasyBot;
import ru.abondin.hreasy.telegram.conf.I18Helper;
import ru.abondin.hreasy.telegram.conf.JwtUtil;

import java.time.Duration;

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
                .action(ctx -> prepareTokenAndDoAction(bot, webClient, ctx))
                .build();
    }

    private void prepareTokenAndDoAction(HrEasyBot bot, WebClient webClient, MessageContext ctx) {
        jwtUtil.putJwtTokenToContext(ctx).flatMap(c -> doAction(bot, ctx)).block(Duration.ofSeconds(20));
    }

    protected abstract <T> Mono<T> doAction(HrEasyBot bot, MessageContext ctx);


}
