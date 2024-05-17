package ru.abondin.hreasy.platform.tg;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;

/**
 * Static Markdown articles (news for example)
 */
@RestController()
@RequestMapping("/internal/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TelegramBotController {

    @Operation(summary = "Get all articles with content. Exclude archived and moderated")
    @GetMapping(value = "test")
    public Mono<String> checkConnection() {
        return AuthHandler.currentAuth().map(authContext -> {
            log.info("Testing telegram bot API with {}", authContext);
            return "Hello " + authContext.getUsername();
        });
    }
}
