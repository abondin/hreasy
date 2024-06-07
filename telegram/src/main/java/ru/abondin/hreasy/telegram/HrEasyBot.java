package ru.abondin.hreasy.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import ru.abondin.hreasy.telegram.abilities.ConfirmAccountActionHandler;
import ru.abondin.hreasy.telegram.abilities.StartMenuAbilityFactory;
import ru.abondin.hreasy.telegram.common.HrEasyActionHandler;
import ru.abondin.hreasy.telegram.common.I18Helper;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

@Component
@Slf4j
public class HrEasyBot extends AbilityBot {

    private final long creatorId;


    private final I18Helper i18;
    private final StartMenuAbilityFactory startMenuAction;
    private final ConfirmAccountActionHandler confirmAccountActionHandler;

    protected HrEasyBot(HrEasyBotProps props, I18Helper i18
            , StartMenuAbilityFactory startMenuAction
            , ConfirmAccountActionHandler confirmAccountAbilityFactory) {
        super(props.getBotToken(), props.getBotUsername());
        this.creatorId = props.getBotCreator();
        this.i18 = i18;
        this.startMenuAction = startMenuAction;
        this.confirmAccountActionHandler = confirmAccountAbilityFactory;
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


    public Ability confirmAccount() {
        return Ability.builder()
                .name("confirm_account")
                .info("Confirm Account in HR Easy")
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .enableStats()
                .action(ctx -> confirmAccountActionHandler.handle(this, new HrEasyActionHandler.HrEasyMessageContext(ctx.user().getUserName(), ctx.chatId())))
                .build();
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
