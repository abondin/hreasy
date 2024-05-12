package ru.abondin.hreasy.telegram.common;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.telegram.abilitybots.api.db.DBContext;
import org.telegram.abilitybots.api.db.MapDBContext;
import ru.abondin.hreasy.telegram.conf.HrEasyBotProps;

@Configuration
public class HrEasyBotConfig {

    @Bean
    DBContext dbContext(HrEasyBotProps props) {
        return MapDBContext.onlineInstance(props.getBotUsername());
    }
}
