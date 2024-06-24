package ru.abondin.hreasy.telegram.abilities;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.abondin.hreasy.telegram.common.I18Helper;

import java.util.List;

@Service
@RequiredArgsConstructor
public class StartMenuInlineKeyboardBuilder {
    private final I18Helper i18n;

    public InlineKeyboardMarkup startMenu() {
        return InlineKeyboardMarkup
                .builder()
                .keyboardRow(List.of(InlineKeyboardButton
                        .builder()
                        .text(i18n.localize("bot.start.menu.FIND_EMPLOYEE"))
                        .callbackData("/find")
                        .build()
                ))
                .keyboardRow(
                        List.of(
                                InlineKeyboardButton
                                        .builder()
                                        .text(i18n.localize("bot.start.menu.MY_PROFILE"))
                                        .callbackData("/my_profile")
                                        .build()
                                , InlineKeyboardButton
                                        .builder()
                                        .text(i18n.localize("bot.start.menu.ABOUT"))
                                        .callbackData("about_bot")
                                        .build()
                        )
                )
                .build();
    }
}
