package ru.abondin.hreasy.telegram.abilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Flag;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.telegram.HrEasyBot;
import ru.abondin.hreasy.telegram.common.HrEasyActionHandler;
import ru.abondin.hreasy.telegram.common.I18Helper;
import ru.abondin.hreasy.telegram.common.JwtUtil;
import ru.abondin.hreasy.telegram.common.ResponseTemplateProcessor;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

@Component
@Slf4j
public class StartMenuAbilityFactory extends HrEasyActionHandler {
    private final StartMenuInlineKeyboardBuilder keyboardBuilder;
    private final MyProfileActionHandler actionHandler;

    public StartMenuAbilityFactory(I18Helper i18n, WebClient webClient, JwtUtil jwtUtil,
                                   ResponseTemplateProcessor templateStorage,
                                   HrEasyBotProps props,
                                   StartMenuInlineKeyboardBuilder keyboardBuilder, MyProfileActionHandler actionHandler) {
        super(i18n, webClient, jwtUtil, templateStorage, props);
        this.keyboardBuilder = keyboardBuilder;
        this.actionHandler = actionHandler;
    }


    public Ability create(HrEasyBot bot) {
        return Ability.builder()
                .name("start")
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
                    log.info("Reply to start menu {}", upd);
                    if (upd.getCallbackQuery() != null) {
                        var callbackQuery = upd.getCallbackQuery();
                        switch (callbackQuery.getData()) {
                            case "/my_profile":
                                actionHandler.handle(b, new HrEasyMessageContext(callbackQuery.getFrom().getUserName()
                                        , callbackQuery.getMessage().getChatId()));
                                break;
                            default:
                                bot.silent().send(i18n.localize("bot.start.menu.unknown_callback", upd.getCallbackQuery().getData()), upd.getMessage().getChatId());
                        }
                    }
                }, Flag.CALLBACK_QUERY)
                .build();
    }
}

