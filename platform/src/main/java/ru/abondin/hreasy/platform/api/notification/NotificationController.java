package ru.abondin.hreasy.platform.api.notification;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.notification.NotificationService;
import ru.abondin.hreasy.platform.service.notification.dto.NotificationDto;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/notifications")
public class NotificationController {
    private final NotificationService service;

    @Operation(summary = "Get current employee notifications")
    @GetMapping("/my")
    public Flux<NotificationDto> myNotifications() {
        return AuthHandler.currentAuth().flatMapMany(service::myNotifications);
    }

    @Operation(summary = "Get current employee unread notification count")
    @GetMapping("/my/unread-count")
    public Mono<Integer> myUnreadCount() {
        return AuthHandler.currentAuth().flatMap(service::myUnreadCount);
    }

    @Operation(summary = "Acknowledge current employee notification")
    @PostMapping("/{notificationId}/acknowledge")
    public Mono<Integer> acknowledge(@PathVariable int notificationId) {
        return AuthHandler.currentAuth().flatMap(auth -> service.acknowledge(auth, notificationId));
    }

    @Operation(summary = "Archive current employee notification")
    @PostMapping("/{notificationId}/archive")
    public Mono<Integer> archive(@PathVariable int notificationId) {
        return AuthHandler.currentAuth().flatMap(auth -> service.archive(auth, notificationId));
    }
}
