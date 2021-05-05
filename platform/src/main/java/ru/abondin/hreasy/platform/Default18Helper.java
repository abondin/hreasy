package ru.abondin.hreasy.platform;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class Default18Helper implements I18Helper {
    private final MessageSource messageSource;

    @Override
    public String localize(String code, Object... args) {
        try {
            return messageSource.getMessage(code, args, new Locale("RU"));
        } catch (NoSuchMessageException ex) {
            log.error("Unsupported localization code {}", code);
            return code;
        }
    }
}
