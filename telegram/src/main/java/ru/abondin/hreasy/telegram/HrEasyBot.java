package ru.abondin.hreasy.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.abondin.hreasy.telegram.abilities.*;
import ru.abondin.hreasy.telegram.common.HrEasyActionHandler;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

import java.util.function.Predicate;

@Component
@Slf4j
public class HrEasyBot extends AbilityBot {

    private final long creatorId;


    private final StartMenuAbilityActionHandler startMenuAction;
    private final ConfirmAccountActionHandler confirmAccountActionHandler;
    private final MyProfileActionHandler myProfileActionHandler;
    private final FindEmployeeActionHandler findEmployeeActionHandler;
    private final SupportActionHandler supportActionHandler;

    protected HrEasyBot(HrEasyBotProps props
            , StartMenuAbilityActionHandler startMenuAction
            , ConfirmAccountActionHandler confirmAccountAbilityFactory, MyProfileActionHandler myProfileActionHandler, FindEmployeeActionHandler findEmployeeActionHandler, SupportActionHandler supportActionHandler) {
        super(props.getBotToken(), props.getBotUsername());
        this.creatorId = props.getBotCreator();
        this.startMenuAction = startMenuAction;
        this.confirmAccountActionHandler = confirmAccountAbilityFactory;
        this.myProfileActionHandler = myProfileActionHandler;
        this.findEmployeeActionHandler = findEmployeeActionHandler;
        this.supportActionHandler = supportActionHandler;
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
                .name(confirmAccountActionHandler.commandName())
                .info("Confirm Account in HR Easy")
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .enableStats()
                .action(ctx -> confirmAccountActionHandler.handle(this, HrEasyActionHandler.hrEasyMessageContext(ctx)))
                .build();
    }

    public Ability find() {
        return Ability.builder()
                .name(findEmployeeActionHandler.commandName())
                .info("Find Employees")
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .enableStats()
                .action(ctx -> {
                    if (ctx.arguments() == null || ctx.arguments().length == 0) {
                        findEmployeeActionHandler.replySendForceReply(this, HrEasyActionHandler.hrEasyMessageContext(ctx));
                    } else {
                        findEmployeeActionHandler.handleReply(this, String.join(" ", ctx.arguments())
                                , HrEasyActionHandler.hrEasyMessageContext(ctx));
                    }
                })
                .reply((b, upd) -> findEmployeeActionHandler
                                .handleReply(b, upd.getMessage().getText(), HrEasyActionHandler.hrEasyMessageContext(upd))
                        , Flag.MESSAGE, Flag.TEXT, Flag.REPLY, isReplyToBot(), findEmployeeActionHandler.isReplyToCommand())
                .reply((b, upd) -> findEmployeeActionHandler.handleCallback(b, upd), Flag.CALLBACK_QUERY)
                .build();
    }

    public Ability myProfile() {
        return Ability.builder()
                .name(myProfileActionHandler.commandName())
                .info("My Profile")
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .enableStats()
                .action(ctx -> myProfileActionHandler.handle(this, HrEasyActionHandler.hrEasyMessageContext(ctx)))
                .build();
    }

    public Ability support() {
        return Ability.builder()
                .name(supportActionHandler.commandName())
                .info("Request for the support")
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .enableStats()
                .action(ctx ->
                        supportActionHandler.startSupportRequestSession(this, HrEasyActionHandler.hrEasyMessageContext(ctx))
                )
                .reply((b, upd) -> supportActionHandler
                                .handleReply(b, HrEasyActionHandler.hrEasyMessageContext(upd), upd.getMessage().getText())
                        , Flag.MESSAGE, Flag.TEXT, Flag.REPLY, isReplyToBot(), supportActionHandler.isReplyToCommand())
                .reply((b, upd) -> supportActionHandler.handleCallback(b, upd), Flag.CALLBACK_QUERY)
                .build();
    }


    private Predicate<Update> isReplyToBot() {
        return upd -> upd.getMessage().getReplyToMessage().getFrom().getUserName().equalsIgnoreCase(getBotUsername());
    }
}
