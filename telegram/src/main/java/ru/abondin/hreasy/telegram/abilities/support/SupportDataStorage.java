package ru.abondin.hreasy.telegram.abilities.support;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import reactor.util.function.Tuple2;
import reactor.util.function.Tuples;
import ru.abondin.hreasy.telegram.abilities.support.dto.TgSupportRequestGroupDto;
import ru.abondin.hreasy.telegram.common.HrEasyActionHandler;
import ru.abondin.hreasy.telegram.common.HttpRequestHelper;
import ru.abondin.hreasy.telegram.common.I18Helper;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

import java.time.OffsetDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

@Service
@RequiredArgsConstructor
@Slf4j
public class SupportDataStorage {
    private final static String DB_GROUPS_CACHE_KEY = "db_support_group_cache";
    private final static String DB_SESSIONS_KEY = "db_support_sessions";

    private final WebClient webClient;
    private final HrEasyBotProps props;
    private final I18Helper i18n;
    private final HttpRequestHelper httpHelper;

    public List<TgSupportRequestGroupDto> groups(BaseAbilityBot bot, HrEasyActionHandler.HrEasyMessageContext ctx) {
        List<TgSupportRequestGroupDto> result;
        if (bot.db().contains(DB_GROUPS_CACHE_KEY)) {
            Tuple2<OffsetDateTime, List<TgSupportRequestGroupDto>> cache = (Tuple2<OffsetDateTime, List<TgSupportRequestGroupDto>>) bot.db().getVar(DB_GROUPS_CACHE_KEY).get();
            if (cache.getT1().isAfter(OffsetDateTime.now())) {
                log.debug("Returning all support request groups from the cache");
                return cache.getT2();
            }
        }
        var httpRequest = webClient
                .get()
                .uri("/api/v1/support/request/groups")
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToFlux(TgSupportRequestGroupDto.class)
                .collectList().doOnSuccess(groups ->
                        bot.db().getVar(DB_GROUPS_CACHE_KEY).set(Tuples.of(OffsetDateTime.now().plus(props.getDefaultCacheTtl()), groups)));
        log.debug("Requesting support request groups from the server");
        result = httpHelper.execHttpSync(httpRequest, bot, ctx);
        result.sort(Comparator.comparing(TgSupportRequestGroupDto::getDisplayName));
        return result;
    }

    public TgSupportRequestGroupDto group(BaseAbilityBot bot, HrEasyActionHandler.HrEasyMessageContext ctx, String key) {
        return groups(bot, ctx).stream().filter(g -> g.getKey().endsWith(key)).findFirst().orElseThrow();
    }

    public void startSession(BaseAbilityBot bot, HrEasyActionHandler.HrEasyMessageContext ctx) {
        Map<Long, SupportRequestSession> cache = bot.db().getMap(DB_SESSIONS_KEY);
        cache.put(ctx.chatId(), new SupportRequestSession());
        bot.db().commit();
    }

    public <R> R doInSession(BaseAbilityBot bot, HrEasyActionHandler.HrEasyMessageContext ctx, Function<SupportRequestSession, R> action) {
        var session = getSession(bot, ctx);
        var result = action.apply(session);
        bot.db().getMap(DB_SESSIONS_KEY).put(ctx.chatId(), session);
        bot.db().commit();
        return result;
    }

    public SupportRequestSession getSession(BaseAbilityBot bot, HrEasyActionHandler.HrEasyMessageContext ctx) {
        var session = (SupportRequestSession) bot.db().getMap(DB_SESSIONS_KEY).get(ctx.chatId());
        if (session == null) {
            bot.silent().send(i18n.localize("bot.support.no_session"), ctx.chatId());
            throw new IllegalArgumentException("Session for chat " + ctx.chatId() + " not found");
        }
        return session;
    }
}
