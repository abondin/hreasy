package ru.abondin.hreasy.telegram.abilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import ru.abondin.hreasy.telegram.abilities.support.SupportDataStorage;
import ru.abondin.hreasy.telegram.abilities.support.SupportGroupsMenuInlineKeyboardBuilder;
import ru.abondin.hreasy.telegram.abilities.support.SupportRequestSession;
import ru.abondin.hreasy.telegram.abilities.support.dto.TgNewSupportRequestDto;
import ru.abondin.hreasy.telegram.common.HrEasyActionHandler;
import ru.abondin.hreasy.telegram.common.HttpRequestHelper;
import ru.abondin.hreasy.telegram.common.I18Helper;
import ru.abondin.hreasy.telegram.common.ResponseTemplateProcessor;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

@Service
@Slf4j
public class SupportActionHandler extends HrEasyActionHandler {

    public static final String COMMAND_NAME = "support";

    private final SupportGroupsMenuInlineKeyboardBuilder keyboardBuilder;
    private final SupportDataStorage dataStorage;

    public SupportActionHandler(I18Helper i18n, WebClient webClient, HttpRequestHelper httpHelper, ResponseTemplateProcessor templateStorage, HrEasyBotProps props, SupportGroupsMenuInlineKeyboardBuilder keyboardBuilder, SupportDataStorage dataStorage) {
        super(i18n, webClient, httpHelper, templateStorage, props);
        this.keyboardBuilder = keyboardBuilder;
        this.dataStorage = dataStorage;
    }

    public static String groupSelected(String key) {
        return "/support/group" + PAYLOAD_SEPARATOR + key;
    }

    public static String categorySelected(String key) {
        return "/support/category" + PAYLOAD_SEPARATOR + key;
    }

    private final static String PAYLOAD_SEPARATOR = " ";


    @Override
    public String commandName() {
        return COMMAND_NAME;
    }

    /**
     * 1. Show all groups to select from
     *
     * @param bot
     * @param ctx
     */
    public void startSupportRequestSession(BaseAbilityBot bot, HrEasyMessageContext ctx) {
        dataStorage.startSession(bot, ctx);
        log.info("Start new session to request a support {}:{}", ctx.accountName(), ctx.chatId());
        showGroupMenu(bot, ctx);
    }

    public void handleCallback(BaseAbilityBot b, Update upd) {
        log.debug("Support action handler. Handle callback {}", upd);
        if (upd.getCallbackQuery() != null) {
            var callbackQuery = upd.getCallbackQuery();
            if (callbackQuery.getData() != null) {
                var data = callbackQuery.getData();
                var parts = data.split(PAYLOAD_SEPARATOR, 2);
                if (parts.length == 2 && parts[0].equals("/support/group")) {
                    var group = parts[1];
                    setGroupAndShowCategoriesMenu(b, hrEasyMessageContext(callbackQuery), group);
                } else if (parts.length == 2 && parts[0].equals("/support/category")) {
                    var category = parts[1];
                    setCategoryAndAskRequestText(b, hrEasyMessageContext(callbackQuery), category);
                } else if (data.startsWith("/support/")){
                    b.silent().send(i18n.localize("bot.support.unsupported.command", data), callbackQuery.getMessage().getChatId());
                }
            }
        }
    }


    private void showGroupMenu(BaseAbilityBot bot, HrEasyMessageContext ctx) {
        log.info("Show group menu for session {}:{}", ctx.accountName(), ctx.chatId());
        var groups = dataStorage.groups(bot, ctx);
        if (groups.isEmpty()) {
            bot.silent().send(i18n.localize("bot.support.no_groups"), ctx.chatId());
        } else {
            var message = SendMessage // Create a message object
                    .builder()
                    .chatId(ctx.chatId())
                    .text(i18n.localize("bot.support.select_group"))
                    .replyMarkup(keyboardBuilder.groups(groups))
                    .build();
            bot.silent().execute(message);
        }
    }

    /**
     * 2. Show all categories of the group to choose from
     *
     * @param bot
     * @param ctx
     */
    public void setGroupAndShowCategoriesMenu(BaseAbilityBot bot, HrEasyMessageContext ctx, String groupKey) {
        log.info("Selected group {} for session {}:{}", groupKey, ctx.accountName(), ctx.chatId());
        var message = dataStorage.doInSession(bot, ctx, session -> {
            var group = dataStorage.group(bot, ctx, groupKey);
            session.setGroup(group);
            return SendMessage
                    .builder()
                    .chatId(ctx.chatId())
                    .text(i18n.localize("bot.support.select_category"))
                    .replyMarkup(keyboardBuilder.categories(session))
                    .build();
        });
        bot.silent().execute(message);
    }


    private void setCategoryAndAskRequestText(BaseAbilityBot bot, HrEasyMessageContext ctx, String category) {
        log.info("Selected category {} for session {}:{}", category, ctx.accountName(), ctx.chatId());
        dataStorage.doInSession(bot, ctx, session -> {
                    if (session.getGroup() == null) {
                        bot.silent().send(i18n.localize("bot.support.no_session"), ctx.chatId());
                        throw new IllegalArgumentException("Group for chat " + ctx.chatId() + " not set");
                    }
                    session.setCategory(category);
                    return null;
                }
        );
        bot.silent().forceReply(replyText(), ctx.chatId());
    }


    public void handleReply(BaseAbilityBot bot, HrEasyMessageContext ctx, String text) {
        SupportRequestSession session = dataStorage.getSession(bot, ctx);
        if (session.getGroup() == null) {
            bot.silent().send(i18n.localize("bot.support.no_session"), ctx.chatId());
            throw new IllegalArgumentException("Group for chat " + ctx.chatId() + " not set");
        }
        if (session.getCategory() == null) {
            bot.silent().send(i18n.localize("bot.support.no_session"), ctx.chatId());
            throw new IllegalArgumentException("Group for chat " + ctx.chatId() + " not set");
        }

        log.info("Post new request from {}:{}. Session={}. Text={}", ctx.accountName(), ctx.chatId(), session, text);
        var body = new TgNewSupportRequestDto(session.getGroup().getKey(), session.getCategory(), text);
        var httpRequest = webClient
                .post()
                .uri("/api/v1/support/request")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(body)
                .retrieve()
                .bodyToMono(Integer.class)
                .map(requestId -> {
                    log.info("New request from {}:{}. Session={} created. ID={}", ctx.accountName(), ctx.chatId(), requestId);
                    bot.silent().send(i18n.localize("bot.support.created", requestId), ctx.chatId());
                    return "OK";
                });
        bot.silent().send(i18n.localize("bot.support.sending"), ctx.chatId());
        httpHelper.execHttpSync(httpRequest, bot, ctx);
    }

    private String replyText() {
        return replyPrefix(commandName()) +
                " " + i18n.localize("bot.support.message.promt");
    }


}
