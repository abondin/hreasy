package ru.abondin.hreasy.platform.tg;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class TelegramLinkNormalizerTest {

    @Test
    public void testNormalizeTelegramLink_UsernameWithoutPrefix() {
        // Test case for just username without any prefix
        String username = "just_a_username";
        String expected = "t.me/just_a_username";
        assertEquals(expected, TelegramLinkNormalizer.normalizeTelegramLink(username));
    }

    @Test
    public void testNormalizeTelegramLink_UsernameWithAtSign() {
        // Test case for username with @ sign
        String username = "@username_with_at_sign";
        String expected = "t.me/username_with_at_sign";
        assertEquals(expected, TelegramLinkNormalizer.normalizeTelegramLink(username));
    }

    @Test
    public void testNormalizeTelegramLink_FullHttpLink() {
        // Test case for full http link
        String link = "http://t.me/username_in_http";
        String expected = "t.me/username_in_http";
        assertEquals(expected, TelegramLinkNormalizer.normalizeTelegramLink(link));
    }

    @Test
    public void testNormalizeTelegramLink_FullHttpsLink() {
        // Test case for full https link
        String link = "https://telegram.me/username_in_https";
        String expected = "t.me/username_in_https";
        assertEquals(expected, TelegramLinkNormalizer.normalizeTelegramLink(link));
    }


    @Test
    public void testNormalizeTelegramLink_NullLink() {
        // Null link test case
        String link = null;
        assertNull(TelegramLinkNormalizer.normalizeTelegramLink(link));
    }

}

