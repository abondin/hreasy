package ru.abondin.hreasy.telegram.common;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.util.Assert;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.abilitybots.api.objects.MessageContext;
import org.telegram.telegrambots.meta.api.objects.CallbackQuery;
import org.telegram.telegrambots.meta.api.objects.Update;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

import java.util.Optional;
import java.util.concurrent.TimeoutException;
import java.util.function.Predicate;

/**
 * Check HR Easy JWT token in bot database.
 * Requests new if not found
 */
@RequiredArgsConstructor
@Slf4j
public abstract class HrEasyActionHandler {

    private final static String REPLY_PREFIX = ">> ";

    protected final I18Helper i18n;
    protected final WebClient webClient;
    protected final HttpRequestHelper httpHelper;
    protected final ResponseTemplateProcessor templateStorage;
    protected final HrEasyBotProps props;

    public static HrEasyMessageContext hrEasyMessageContext(MessageContext ctx) {
        Assert.notNull(ctx, "Message context not found");
        Assert.hasText(Optional.ofNullable(ctx.user()).map(u -> u.getUserName()).orElse(null), "User name not found");
        Assert.notNull(ctx.chatId(), "Chat id not found");
        return new HrEasyMessageContext(ctx.user().getUserName(), ctx.chatId());
    }

    public static HrEasyMessageContext hrEasyMessageContext(Update update) {
        Assert.notNull(update, "Update is null");
        Assert.notNull(update.getMessage(), "Message is null for update " + update);
        Assert.hasText(Optional.ofNullable(update.getMessage()).map(u -> u.getFrom()).map(u -> u.getUserName()).orElse(null), "User name not found");
        Assert.notNull(update.getMessage().getChatId(), "Chat id not found");
        return new HrEasyMessageContext(update.getMessage().getFrom().getUserName(), update.getMessage().getChatId());
    }

    public static HrEasyMessageContext hrEasyMessageContext(CallbackQuery callbackQuery) {
        Assert.notNull(callbackQuery, "Callback query not found");
        Assert.notNull(callbackQuery.getMessage(), "Message not found in callback query " + callbackQuery);
        Assert.notNull(callbackQuery.getFrom(), "From not found in callback query " + callbackQuery);
        Assert.hasText(Optional.ofNullable(callbackQuery.getFrom()).map(u -> u.getUserName()).orElse(null), "User name not found in from section of callback query");
        Assert.notNull(callbackQuery.getMessage().getChatId(), "Chat id not found");
        return new HrEasyMessageContext(callbackQuery.getFrom().getUserName()
                , callbackQuery.getMessage().getChatId());
    }

    public abstract String commandName();

    public record HrEasyMessageContext(String accountName, long chatId) {

    }



    protected String replyPrefix(String command) {
        return REPLY_PREFIX + command + ": ";
    }

    public Predicate<Update> isReplyToCommand() {
        return u -> {
            var isReply = u.getMessage() != null
                    && u.getMessage().getReplyToMessage() != null
                    && u.getMessage().getReplyToMessage().getText() != null
                    && u.getMessage().getReplyToMessage().getText().startsWith(REPLY_PREFIX + commandName());
            return isReply;
        };
    }

}
