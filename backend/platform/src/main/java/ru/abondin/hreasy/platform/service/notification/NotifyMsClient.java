package ru.abondin.hreasy.platform.service.notification;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.config.HrEasyNotificationDeliveryProps;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotifyMsClient implements InitializingBean {
    private final HrEasyNotificationDeliveryProps props;
    private final WebClient.Builder webClientBuilder;

    private WebClient webClient;

    @Override
    public void afterPropertiesSet() {
        if (!props.isEnabled()) {
            return;
        }
        if (props.getBaseUrl() == null) {
            throw new IllegalStateException("hreasy.notifications.delivery-service.base-url must be configured");
        }
        if (!StringUtils.hasText(props.getToken())) {
            throw new IllegalStateException("hreasy.notifications.delivery-service.token must be configured");
        }
        webClient = webClientBuilder
                .baseUrl(props.getBaseUrl().toString())
                .defaultHeader(HttpHeaders.AUTHORIZATION, "Bearer " + props.getToken())
                .build();
    }

    public Mono<Void> sendBestEffort(CreateNotificationRequest request) {
        if (!props.isEnabled()) {
            return Mono.empty();
        }
        return webClient.post()
                .uri("/api/v1/notifications")
                .bodyValue(request)
                .retrieve()
                .bodyToMono(CreateNotificationResponse.class)
                .timeout(props.getTimeout())
                .doOnNext(response -> log.info("notify-ms accepted event {} with id {}",
                        request.eventType(), response.notificationId()))
                .then()
                .onErrorResume(error -> {
                    log.warn("Unable to publish event {} with dedupe key {} to notify-ms",
                            request.eventType(), request.dedupeKey(), error);
                    return Mono.empty();
                });
    }

    public record CreateNotificationRequest(
            String eventType,
            Recipient recipient,
            String priority,
            String dedupeKey,
            String locale,
            String title,
            String body,
            String data
    ) {
    }

    public record Recipient(
            String type,
            String login,
            String chatId,
            Integer employeeId
    ) {
    }

    private record CreateNotificationResponse(
            Integer notificationId,
            String status
    ) {
    }
}
