package ru.abondin.hreasy.notifyms.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.notifyms.service.NotificationAcceptService;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationAcceptService service;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<CreateNotificationResponse> create(@Valid @RequestBody CreateNotificationRequest request) {
        return service.accept(request)
                .map(id -> new CreateNotificationResponse(id, "accepted"));
    }
}
