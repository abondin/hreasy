package ru.abondin.hreasy.platform.tg;


import org.apache.commons.lang3.StringUtils;


public class TelegramLinkNormalizer {

    /**
     * Method to normalize a Telegram account link
     */
    public static String normalizeTelegramLink(String link) {
        // Check if the link is null, empty, or blank
        if (StringUtils.isBlank(link)) {
            return null;
        }

        // Split the link by "/"
        String[] parts = link.trim().split("/");

        // Get the last part of the split link (which should be the username)
        String username = parts[parts.length - 1];

        // Remove "@" if present at the beginning of the username
        if (username.startsWith("@")) {
            username = username.substring(1);
        }

        return "t.me/" + username;
    }
}
