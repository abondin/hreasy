package ru.abondin.hreasy.telegram.abilities;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import org.telegram.telegrambots.meta.api.methods.send.SendMessage;
import org.telegram.telegrambots.meta.api.objects.Update;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.InlineKeyboardMarkup;
import org.telegram.telegrambots.meta.api.objects.replykeyboard.buttons.InlineKeyboardButton;
import ru.abondin.hreasy.telegram.abilities.dto.TgFindEmployeeRequest;
import ru.abondin.hreasy.telegram.abilities.dto.TgFindEmployeeResponse;
import ru.abondin.hreasy.telegram.common.HrEasyActionHandler;
import ru.abondin.hreasy.telegram.common.HttpRequestHelper;
import ru.abondin.hreasy.telegram.common.I18Helper;
import ru.abondin.hreasy.telegram.common.ResponseTemplateProcessor;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

import java.util.List;

/**
 * Find employee by email or display name
 */
@Service
@Slf4j
public class FindEmployeeActionHandler extends HrEasyActionHandler {

    public FindEmployeeActionHandler(I18Helper i18n, WebClient webClient, HttpRequestHelper httpHelper, ResponseTemplateProcessor templateStorage,
                                     HrEasyBotProps props) {
        super(i18n, webClient, httpHelper, templateStorage, props);
    }

    public static final String COMMAND_NAME = "find";

    public void handleReply(BaseAbilityBot bot, String queryMessage, HrEasyMessageContext ctx) {
        log.info("Sending find employee request. Context: {}, query: {}", ctx, queryMessage);
        var request = webClient
                .post()
                .uri("/api/v1/employee/find")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new TgFindEmployeeRequest(queryMessage, props.getDefaultFetchSize()))
                .retrieve()
                .bodyToMono(TgFindEmployeeResponse.class)
                .map(res -> {
                    if (res.getEmployees().isEmpty()) {
                        var message = templateStorage.process("find_employees_not_found", c -> {
                        });
                        bot.silent().send(message, ctx.chatId());
                    } else if (res.getEmployees().size() == 1) {
                        var message = templateStorage.process("find_employees_details", c -> c.setVariable("e", res.getEmployees().getFirst()));
                        bot.silent().send(message, ctx.chatId());
                    } else {
                        employeeSelection(res, bot, ctx);
                    }
                    return "OK";
                });
        httpHelper.execHttpSync(request, bot, ctx);
    }


    private void employeeSelection(TgFindEmployeeResponse res, BaseAbilityBot bot, HrEasyMessageContext ctx) {
        var inlineBuilder = InlineKeyboardMarkup
                .builder();
        var text = i18n.localize("bot.find.response.select");
        if (res.isHasMore()) {
            text += "\n\n" + i18n.localize("bot.find.response.select_more");
        }
        res.getEmployees().forEach(e ->
                inlineBuilder
                        .keyboardRow(List.of(InlineKeyboardButton
                                .builder()
                                .text(e.displayName() + " (" + e.email() + ")")
                                .callbackData("/find/id " + e.id())
                                .build()))
        );
        var message = SendMessage // Create a message object
                .builder()
                .chatId(ctx.chatId())
                .text(text)
                .replyMarkup(inlineBuilder.build())
                .build();
        bot.silent().execute(message);
    }

    @Override
    public String commandName() {
        return COMMAND_NAME;
    }

    public void replySendForceReply(BaseAbilityBot bot, HrEasyMessageContext ctx) {
        bot.silent().forceReply(replyText(), ctx.chatId());
    }

    private String replyText() {
        return replyPrefix(commandName()) + i18n.localize("bot.find.query.promt");
    }

    public void handleCallback(BaseAbilityBot b, Update upd) {
        if (upd.getCallbackQuery() != null) {
            var callbackQuery = upd.getCallbackQuery();
            if (callbackQuery.getData() != null) {
                var data = callbackQuery.getData();
                var parts = data.split("\\s+");
                if (parts.length == 2 && parts[0].equals("/find/id")) {
                    var id = parts[1];
                    getInformationById(id, b, hrEasyMessageContext(callbackQuery));
                }
            }
        }
    }

    private void getInformationById(String id, BaseAbilityBot b, HrEasyMessageContext ctx) {
        log.info("Sending find employee by id. Context: {}, id: {}", ctx, id);
        var request = webClient
                .post()
                .uri("/api/v1/employee/find/" + id)
                .accept(MediaType.APPLICATION_JSON)
                .retrieve()
                .bodyToMono(TgFindEmployeeResponse.EmployeeDto.class)
                .map(e -> {
                    var message = templateStorage.process("find_employees_details", c -> c.setVariable("e", e));
                    b.silent().send(message, ctx.chatId());
                    return "OK";
                });
        httpHelper.execHttpSync(request, b, ctx);
    }
}
