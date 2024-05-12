package ru.abondin.hreasy.telegram.abilities;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.abilitybots.api.objects.Privacy;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.abondin.hreasy.telegram.HrEasyBot;
import ru.abondin.hreasy.telegram.common.HrEasyAbilityFactory;
import ru.abondin.hreasy.telegram.conf.I18Helper;

import java.util.List;
import java.util.function.Consumer;

@Component
@RequiredArgsConstructor
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
                .action(action(bot)).build();
    }

    @Override
    public String name() {
        return "start";
    }

    public Consumer<MessageContext> action(HrEasyBot bot) {
        return (ctx) -> {
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
