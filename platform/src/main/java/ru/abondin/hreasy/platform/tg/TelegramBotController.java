package ru.abondin.hreasy.platform.tg;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.EmployeeService;
import ru.abondin.hreasy.platform.tg.dto.TgMapper;
import ru.abondin.hreasy.platform.tg.dto.TgMyProfileDto;

/**
 * Static Markdown articles (news for example)
 */
@RestController()
@RequestMapping("/telegram/api/v1")
@RequiredArgsConstructor
@Slf4j
public class TelegramBotController {
    private final EmployeeService emplService;
    private final TelegramBotConfirmService confirmService;
    private final TgMapper mapper;

    @Operation(summary = "Send employee's telegram account confirmation email")
    @PostMapping(value = "confirm/start")
    public Mono<String> sendConfirmationLink() {
        return AuthHandler.currentAuth().flatMap(auth ->
                confirmService.sendConfirmationLink(auth));
    }

    @Operation(summary = "Confirm employee's telegram account")
    @PostMapping(value = "confirm/complete/{confirmationCode}")
    public Mono<Integer> confirmTelegramAccount(@PathVariable String confirmationCode) {
        return AuthHandler.currentAuth().flatMap(auth ->
                confirmService.confirm(auth, confirmationCode));
    }

    @Operation(summary = "Get my information")
    @GetMapping(value = "my-profile")
    public Mono<TgMyProfileDto> myProfile() {
        return AuthHandler.currentAuth().flatMap(auth ->
                        emplService.find(auth.getEmployeeInfo().getEmployeeId(), auth))
                .map(mapper::myProfile);
    }
}
