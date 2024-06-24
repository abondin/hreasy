package ru.abondin.hreasy.telegram.abilities;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.telegram.abilitybots.api.bot.BaseAbilityBot;
import ru.abondin.hreasy.telegram.abilities.dto.FindEmployeeRequest;
import ru.abondin.hreasy.telegram.abilities.dto.FindEmployeeResponse;
import ru.abondin.hreasy.telegram.common.HrEasyActionHandler;
import ru.abondin.hreasy.telegram.common.I18Helper;
import ru.abondin.hreasy.telegram.common.JwtUtil;
import ru.abondin.hreasy.telegram.common.ResponseTemplateProcessor;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

/**
 * Find employee by email or display name
 */
@Service
public class FindEmployeeActionHandler extends HrEasyActionHandler {

    private final static String FIND_EMPLOYEE_COMMAND = "find";
    private static final Logger log = LoggerFactory.getLogger(FindEmployeeActionHandler.class);

    public FindEmployeeActionHandler(I18Helper i18n, WebClient webClient, JwtUtil jwtUtil, ResponseTemplateProcessor templateStorage,
                                     HrEasyBotProps props) {
        super(i18n, webClient, jwtUtil, templateStorage, props);
    }


    public void handleReply(BaseAbilityBot bot, String queryMessage, HrEasyMessageContext ctx) {
        log.info("Sending find employee request. Context: {}, query: {}", ctx, queryMessage);
        var request = webClient
                .post()
                .uri("/api/v1/employee/find")
                .accept(MediaType.APPLICATION_JSON)
                .bodyValue(new FindEmployeeRequest(queryMessage, 3))
                .retrieve()
                .bodyToMono(FindEmployeeResponse.class)
                .map(res -> {
                    String message;
                    if (res.getEmployees().isEmpty()) {
                        message = templateStorage.process("find_employees_not_found", c -> {
                        });
                    } else if (res.getEmployees().size() == 1) {
                        message = templateStorage.process("find_employees_details", c -> c.setVariable("e", res.getEmployees().get(0)));
                    } else {
                        message = templateStorage.process("find_employees_multiple", c -> c.setVariable("res", res));
                    }
                    bot.silent().sendMd(message, ctx.chatId());
                    return "OK";
                });
        execHttpSync(request, bot, ctx);
    }


    @Override
    public String commandName() {
        return FIND_EMPLOYEE_COMMAND;
    }

    public void replySendForceReply(BaseAbilityBot bot, HrEasyMessageContext ctx) {
        bot.silent().forceReply(replyText(), ctx.chatId());
    }

    private String replyText() {
        return replyPrefix(commandName()) + i18n.localize("bot.find.query.promt");
    }

}
