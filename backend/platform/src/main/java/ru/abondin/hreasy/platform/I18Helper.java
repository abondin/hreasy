package ru.abondin.hreasy.platform;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.Locale;

/**
 * Deal with localized messages
 */
public interface I18Helper {
    String localize(String code, Object... args);
    String localize(Locale locale, String code, Object... args);

    String formatDate(LocalDate date);
    String formatDate(Locale locale, LocalDate date);
    String formatDateTime(OffsetDateTime dateTime);
    String formatDateTime(Locale locale, OffsetDateTime dateTime);

    class DummyI18Helper implements I18Helper{
        @Override
        public String localize(String code, Object... args) {
            return code;
        }

        @Override
        public String localize(Locale locale, String code, Object... args) {
            return localize( code, args);
        }

        @Override
        public String formatDate(LocalDate date) {
            return date == null ? null : date.toString();
        }

        @Override
        public String formatDate(Locale locale, LocalDate date) {
            return date == null ? null : date.toString();
        }

        @Override
        public String formatDateTime(OffsetDateTime dateTime) {
            return dateTime == null ? null : dateTime.toString();
        }

        @Override
        public String formatDateTime(Locale locale, OffsetDateTime dateTime) {
            return dateTime == null ? null : dateTime.toString();
        }
    }
}
