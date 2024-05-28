package ru.abondin.hreasy.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import ru.abondin.hreasy.telegram.abilities.ConfirmAccountAbilityFactory;
import ru.abondin.hreasy.telegram.abilities.MyProfileAbilityFactory;
import ru.abondin.hreasy.telegram.abilities.StartMenuAbilityFactory;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;
import ru.abondin.hreasy.telegram.common.I18Helper;

@Component
@Slf4j
public class HrEasyBot extends AbilityBot {

    private final long creatorId;


    private final I18Helper i18;
    private final StartMenuAbilityFactory startMenuAction;
    private final MyProfileAbilityFactory myProfileAbilityFactory;
    private final ConfirmAccountAbilityFactory confirmAccountAbilityFactory;

    protected HrEasyBot(HrEasyBotProps props, I18Helper i18
            , StartMenuAbilityFactory startMenuAction
            , MyProfileAbilityFactory myProfileAbilityFactory, ConfirmAccountAbilityFactory confirmAccountAbilityFactory) {
        super(props.getBotToken(), props.getBotUsername());
        this.creatorId = props.getBotCreator();
        this.i18 = i18;
        this.startMenuAction = startMenuAction;
        this.myProfileAbilityFactory = myProfileAbilityFactory;
        this.confirmAccountAbilityFactory = confirmAccountAbilityFactory;
    }

    @Override
    public long creatorId() {
        return creatorId;
    }


    @Override
    public void onClosing() {
        log.info("onClosing:");
        super.onClosing();
    }

    public Ability start() {
        return startMenuAction.create(this);
    }

    public Ability myProfile() {
        return myProfileAbilityFactory.create(this);
    }

    public Ability confirmAccount() {
        return confirmAccountAbilityFactory.create(this);
    }


    public Ability help() {
        return Ability.builder()
                .flag(update -> {
                    log.info("Check flags for {}", update);
                    return true;
                })
                .name("help")
                .info("Help")
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    silent().send("Help me!!", ctx.chatId());
                })
                .build();
    }


}
