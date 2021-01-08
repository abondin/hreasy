package ru.abondin.hreasy.platform;

/**
 * Deal with localized messages
 */
public interface I18Helper {
    String localize(String code, Object... args);
}
