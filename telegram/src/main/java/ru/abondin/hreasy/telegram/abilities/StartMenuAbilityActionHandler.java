package ru.abondin.hreasy.telegram.abilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import ru.abondin.hreasy.telegram.HrEasyBot;
import ru.abondin.hreasy.telegram.common.*;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

@Component
@Slf4j
public class StartMenuAbilityActionHandler extends HrEasyActionHandler {

    public static String menuKey(String command) {
        return "/start_menu/" + command;
    }

    private final StartMenuInlineKeyboardBuilder keyboardBuilder;
    private final MyProfileActionHandler myProfileActionHandler;
    private final FindEmployeeActionHandler findEmployeeActionHandler;
    private final SupportActionHandler supportRequestActionHandler;

    public StartMenuAbilityActionHandler(I18Helper i18n, WebClient webClient,  HttpRequestHelper httpHelper,
                                         ResponseTemplateProcessor templateStorage,
                                         HrEasyBotProps props,
                                         StartMenuInlineKeyboardBuilder keyboardBuilder, MyProfileActionHandler myProfileActionHandler,
                                         FindEmployeeActionHandler findEmployeeActionHandler, SupportActionHandler supportRequestActionHandler) {
        super(i18n, webClient, httpHelper, templateStorage, props);
        this.keyboardBuilder = keyboardBuilder;
        this.myProfileActionHandler = myProfileActionHandler;
        this.findEmployeeActionHandler = findEmployeeActionHandler;
        this.supportRequestActionHandler = supportRequestActionHandler;
    }

    @Override
    public String commandName() {
        return "start";
    }

    public Ability create(HrEasyBot bot) {
        return Ability.builder()
                .name(commandName())
                .info("Start")
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .enableStats()
                .action(ctx -> {
                    SendMessage message = SendMessage // Create a message object
                            .builder()
                            .chatId(ctx.chatId())
                            .text(i18n.localize("bot.start.welcome"))
                            .replyMarkup(keyboardBuilder.startMenu())
                            .build();
                    bot.silent().execute(message);
                })
                .reply((b, upd) -> {
                    if (upd.getCallbackQuery() != null) {
                        var callbackQuery = upd.getCallbackQuery();
                        if (menuKey(myProfileActionHandler.commandName()).equals(callbackQuery.getData())) {
                            myProfileActionHandler.handle(b, hrEasyMessageContext(callbackQuery));
                        } else if (menuKey(findEmployeeActionHandler.commandName()).equals(callbackQuery.getData())) {
                            findEmployeeActionHandler.replySendForceReply(b, hrEasyMessageContext(callbackQuery));
                        } else if (menuKey(supportRequestActionHandler.commandName()).equals(callbackQuery.getData())) {
                            supportRequestActionHandler.startSupportRequestSession(b, hrEasyMessageContext(callbackQuery));
                        }
                    }
                }, Flag.CALLBACK_QUERY)
                .build();
    }
}

