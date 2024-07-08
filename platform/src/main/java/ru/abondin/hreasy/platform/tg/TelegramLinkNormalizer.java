package ru.abondin.hreasy.platform.tg;


import org.apache.commons.lang3.StringUtils;


public class TelegramLinkNormalizer {

    /**
     * Method to normalize a Telegram account link
     */
    public static String normalizeTelegramLink(String link) {
        var username = extractAccountName(link);
        return username == null ? null : ("t.me/" + username);
    }

    /**
     * Extract telegram username from different cases:
     * <ul>
     *     </li>username</li>
     *     </li>@username</li>
     *     </li>https://t.me/username</li>
     * </ul>
     * @param telegramString
     * @return
     */
    public static String extractAccountName(String telegramString) {
        // Check if the link is null, empty, or blank
        if (StringUtils.isBlank(telegramString)) {
            return null;
        }

        // Split the link by "/"
        String[] parts = telegramString.trim().split("/");

        // Get the last part of the split link (which should be the username)
        String username = parts[parts.length - 1];

        // Remove "@" if present at the beginning of the username
        if (username.startsWith("@")) {
            username = username.substring(1);
        }
        return username;
    }
}
