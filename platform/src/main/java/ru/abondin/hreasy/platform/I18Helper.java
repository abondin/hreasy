package ru.abondin.hreasy.platform;

import java.util.Locale;

/**
 * Deal with localized messages
 */
public interface I18Helper {
    String localize(String code, Object... args);
    String localize(Locale locale, String code, Object... args);

    class DummyI18Helper implements I18Helper{
        @Override
        public String localize(String code, Object... args) {
            return code;
        }

        @Override
        public String localize(Locale locale, String code, Object... args) {
            return localize(null, code, args);
        }
    }
}
