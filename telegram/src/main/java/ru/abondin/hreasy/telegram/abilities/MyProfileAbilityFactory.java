package ru.abondin.hreasy.telegram.abilities;

import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.objects.MessageContext;
import ru.abondin.hreasy.telegram.HrEasyBot;
import ru.abondin.hreasy.telegram.common.HrEasyAbilityWithAuthFactory;
import ru.abondin.hreasy.telegram.conf.I18Helper;

@Component
public class MyProfileAbilityFactory extends HrEasyAbilityWithAuthFactory {
    public static final String MY_PROFILE_COMMAND = "my_profile";

    public MyProfileAbilityFactory(I18Helper i18n, DBContext db) {
        super(i18n, db);
    }

    @Override
    public String name() {
        return MY_PROFILE_COMMAND;
    }

    @Override
    protected void doAction(HrEasyBot bot, MessageContext ctx, String accessToken) {
        bot.silent().send("Hello user with access token " + accessToken, ctx.chatId());
    }
}
