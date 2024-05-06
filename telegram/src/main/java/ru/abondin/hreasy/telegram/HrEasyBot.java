package ru.abondin.hreasy.telegram;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.telegram.abilitybots.api.bot.AbilityBot;
import org.telegram.abilitybots.api.objects.Ability;
import org.telegram.abilitybots.api.objects.Locality;
import org.telegram.abilitybots.api.objects.Privacy;

@Component
@Slf4j
public class HrEasyBot extends AbilityBot {

    private final long creatorId;

    protected HrEasyBot(HrEasyBotProps props) {
        super(props.getBotToken(), props.getBotUsername());
        this.creatorId = props.getBotCreator();
    }

    @Override
    public long creatorId() {
        return creatorId;
    }


    @Override
    public void onClosing() {
        log.info("onClosing:");
        super.onClosing();
    }

    public Ability start() {
        return Ability.builder()
                .name("start")
                .info("Start!")
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .enableStats()
                .action(ctx ->
                        silent.send("Welcome", ctx.chatId())
                )
                .build();
    }


    public Ability help() {
        return Ability.builder()
                .flag(update -> {
                    log.info("Check flags for {}", update);
                    return true;
                })
                .name("help")
                .info("Help")
                .input(0)
                .locality(Locality.USER)
                .privacy(Privacy.PUBLIC)
                .action(ctx -> {
                    silent.send("Help me!!", ctx.chatId());
                })
                .build();
    }


}
