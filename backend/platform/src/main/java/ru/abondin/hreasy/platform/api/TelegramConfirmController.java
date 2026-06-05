package ru.abondin.hreasy.platform.api;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.tg.TelegramConfirmService;

@RestController()
@RequestMapping("/api/v1/telegram")
@RequiredArgsConstructor
@Slf4j
public class TelegramConfirmController {

    private final TelegramConfirmService confirmService;

    @Operation(summary = "Confirm employee's telegram account")
    @PostMapping(value = "confirm/{employeeId}/{accountName}/{confirmationCode}")
    public Mono<Integer> confirmTelegramAccount(
            @PathVariable int employeeId,
            @PathVariable String accountName,
            @PathVariable String confirmationCode) {
        return confirmService.confirm(employeeId, accountName, confirmationCode);
    }
}
