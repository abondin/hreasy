package ru.abondin.hreasy.platform;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.MessageSource;
import org.springframework.context.NoSuchMessageException;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.FormatStyle;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class Default18Helper implements I18Helper {
    private final MessageSource messageSource;
    private final Map<Locale, DateTimeFormatter> dateFormatters = new HashMap<>();
    private Locale defaultLocale;

    @PostConstruct
    protected void init() {
        defaultLocale = Locale.getDefault();
        log.info("Initialize i18n with :{}", defaultLocale);
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

    @Override
    public String formatDate(LocalDate date) {
        return formatDate(defaultLocale, date);
    }

    @Override
    public String formatDate(Locale locale, LocalDate date) {
        if (date == null) {
            return null;
        }
        var formatter = dateFormatters.computeIfAbsent(locale, l ->
                DateTimeFormatter.ofLocalizedDate(FormatStyle.SHORT).withLocale(l)
        );
        return formatter.format(date);
    }
}
