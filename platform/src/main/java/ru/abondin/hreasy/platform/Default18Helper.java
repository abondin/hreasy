package ru.abondin.hreasy.platform;

import lombok.RequiredArgsConstructor;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
public class Default18Helper implements I18Helper{
    private final MessageSource messageSource;

    @Override
    public String localize(String code, Object... args) {
        return messageSource.getMessage(code, args, new Locale("ru"));
    }
}
