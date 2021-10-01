package ru.abondin.hreasy.platform;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class Default18Helper implements I18Helper {
    private final MessageSource messageSource;

    @PostConstruct
    protected void init() {
        log.info("Initialize i18n with :{}", Locale.getDefault());
    }

    @Deprecated
    @Override
    public String localize(String code, Object... args) {
        return localize(null, code, args);
    }

    @Override
    public String localize(Locale locale, String code, Object... args) {
        try {
            return messageSource.getMessage(code, args, locale == null ? Locale.getDefault() : locale);
        } catch (NoSuchMessageException ex) {
            log.error("Unsupported localization code {}", code);
            return code;
        }
    }
}
