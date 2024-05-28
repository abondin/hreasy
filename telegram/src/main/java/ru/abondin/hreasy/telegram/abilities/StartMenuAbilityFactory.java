package ru.abondin.hreasy.telegram.abilities;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.objects.*;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.abondin.hreasy.telegram.HrEasyBot;
import ru.abondin.hreasy.telegram.common.HrEasyAbilityFactory;
import ru.abondin.hreasy.telegram.common.I18Helper;

import java.util.List;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
@Slf4j
public class StartMenuAbilityFactory implements HrEasyAbilityFactory {
    private final I18Helper i18n;

    public Ability create(HrEasyBot bot) {
        return Ability.builder()
                .name(name())
                .info("Start")
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .enableStats()
                .action(action(bot))
                .reply((b,upd)-> log.info("Reply to start menu {}", upd), Flag.CALLBACK_QUERY)
                .build();
    }

    @Override
    public String name() {
        return "start";
    }

    public Consumer<MessageContext> action(HrEasyBot bot) {
        return ctx -> {
            SendMessage message = SendMessage // Create a message object
                    .builder()
                    .chatId(ctx.chatId())
                    .text(i18n.localize("bot.start.welcome"))
                    .replyMarkup(InlineKeyboardMarkup
                            .builder()
                            .keyboardRow(
                                    List.of(
                                            InlineKeyboardButton
                                                    .builder()
                                                    .text(i18n.localize("bot.start.menu.CHANGE_PASSWORD"))
                                                    .callbackData("change_password")
                                                    .build()
                                            , InlineKeyboardButton
                                                    .builder()
                                                    .text(i18n.localize("bot.start.menu.MY_PROFILE"))
                                                    .callbackData("/"+MyProfileAbilityFactory.MY_PROFILE_COMMAND)
                                                    .build()
                                            , InlineKeyboardButton
                                                    .builder()
                                                    .text(i18n.localize("bot.start.menu.ABOUT"))
                                                    .callbackData("about_bot")
                                                    .build()
                                    )
                            )
                            .build())
                    .build();
            bot.silent().execute(message);
        };
    }


}
