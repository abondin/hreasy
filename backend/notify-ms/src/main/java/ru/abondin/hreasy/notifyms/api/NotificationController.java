package ru.abondin.hreasy.notifyms.api;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
@Slf4j
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationAcceptService service;

    @PostMapping
    @ResponseStatus(HttpStatus.ACCEPTED)
    public Mono<CreateNotificationResponse> create(@Valid @RequestBody CreateNotificationRequest request) {
        log.info("Accept notification request eventType={}, dedupeKey={}, recipientType={}, recipientLogin={}, employeeId={}",
                request.getEventType(),
                request.getDedupeKey(),
                request.getRecipient().getType(),
                request.getRecipient().getLogin(),
                request.getRecipient().getEmployeeId());
        return service.accept(request)
                .doOnNext(id -> log.info("Notification request accepted id={}, dedupeKey={}", id, request.getDedupeKey()))
                .map(id -> new CreateNotificationResponse(id, "accepted"));
    }
}
