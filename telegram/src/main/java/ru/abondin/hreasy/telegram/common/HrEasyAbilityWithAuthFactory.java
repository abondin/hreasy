package ru.abondin.hreasy.telegram.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import ru.abondin.hreasy.telegram.HrEasyBot;
import ru.abondin.hreasy.telegram.conf.I18Helper;

/**
 * Check HR Easy JWT token in bot database.
 * Requests new if not found
 */
@RequiredArgsConstructor
@Slf4j
public abstract class HrEasyAbilityWithAuthFactory implements HrEasyAbilityFactory {
    private final String ACCESS_TOKENS_KEY = "hrEasyAccessTokens";
    private final I18Helper i18n;
    private final DBContext db;

    public Ability create(HrEasyBot bot) {
        return Ability.builder()
                .name(name())
                .info(name())
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .enableStats()
                .action(ctx -> checkTokenAndDoAction(bot, ctx))
                .build();
    }

    private void checkTokenAndDoAction(HrEasyBot bot, MessageContext ctx) {
        db.<String, String>getMap(ACCESS_TOKENS_KEY).compute(ctx.user().getUserName(), (k, v) -> {
            String token;
            if (v == null) {
                log.debug("Trying to auth user {}", ctx.user().getUserName());
                token = "newToken-" + ctx.user().getUserName() + "-" + System.currentTimeMillis();
            } else {
                token = v;
            }
            doAction(bot, ctx, token);
            return token;
        });
    }

    protected abstract void doAction(HrEasyBot bot, MessageContext ctx, String accessToken);


}
