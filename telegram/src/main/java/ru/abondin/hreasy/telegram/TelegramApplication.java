package ru.abondin.hreasy.telegram;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.telegram.telegrambots.meta.TelegramBotsApi;
import org.telegram.telegrambots.meta.exceptions.TelegramApiException;
import org.telegram.telegrambots.updatesreceivers.DefaultBotSession;

@SpringBootApplication
public class TelegramApplication {

    public static void main(String[] args) {
        var ctx = SpringApplication.run(TelegramApplication.class, args);
        try {
            var botsApi = new TelegramBotsApi(DefaultBotSession.class);
            botsApi.registerBot(ctx.getBean(HrEasyBot.class));
        } catch (TelegramApiException e) {
            throw new RuntimeException(e);
        }
    }

}
