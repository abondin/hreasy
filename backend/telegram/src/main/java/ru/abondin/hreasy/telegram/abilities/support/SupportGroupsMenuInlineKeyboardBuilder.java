package ru.abondin.hreasy.telegram.abilities.support;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.abondin.hreasy.telegram.abilities.SupportActionHandler;
import ru.abondin.hreasy.telegram.abilities.support.dto.TgSupportRequestGroupDto;
import ru.abondin.hreasy.telegram.common.I18Helper;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class SupportGroupsMenuInlineKeyboardBuilder {
    private final I18Helper i18n;

    public InlineKeyboardMarkup groups(List<TgSupportRequestGroupDto> groups) {
        var rows = groups.stream().map(g -> List.of(
                        InlineKeyboardButton.builder()
                                .text(g.getDisplayName())
                                .callbackData(SupportActionHandler.groupSelected(g.getKey()))
                                .build()
                )
        ).collect(Collectors.toList());
        return InlineKeyboardMarkup
                .builder()
                .keyboard(rows)
                .build();
    }

    public InlineKeyboardMarkup categories(SupportRequestSession session) {
        var rows = session.getCategoriesCache().stream().map(category -> List.of(
                        InlineKeyboardButton.builder()
                                .text(category.getT2())
                                .callbackData(SupportActionHandler.categorySelected(category.getT1()))
                                .build()
                )
        ).collect(Collectors.toList());
        return InlineKeyboardMarkup
                .builder()
                .keyboard(rows)
                .build();
    }
}
