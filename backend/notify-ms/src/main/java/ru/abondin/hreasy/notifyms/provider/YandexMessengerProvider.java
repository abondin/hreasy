package ru.abondin.hreasy.notifyms.provider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.reactive.function.client.WebClientRequestException;
import org.springframework.web.reactive.function.client.WebClientResponseException;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.notifyms.config.NotificationProperties;
import ru.abondin.hreasy.notifyms.repo.NotificationDeliveryEntry;
import ru.abondin.hreasy.notifyms.repo.NotificationEntry;
import ru.abondin.hreasy.notifyms.service.RecipientType;

import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class YandexMessengerProvider {
    private final WebClient.Builder webClientBuilder;
    private final NotificationProperties props;

    public Mono<ProviderSendResult> send(NotificationEntry notification, NotificationDeliveryEntry delivery) {
        var config = props.getChannels().getYandexMessenger();
        if (!StringUtils.hasText(config.getOauthToken())) {
            log.warn("Yandex Messenger OAuth token is not configured notificationId={}, deliveryId={}",
                    notification.getId(),
                    delivery.getId());
            return Mono.just(ProviderSendResult.permanent(null, "missing_token", "Yandex Messenger OAuth token is not configured"));
        }

        var request = new HashMap<String, Object>();
        request.put("text", notification.getBody());
        request.put("payload_id", delivery.getProviderPayloadId());

        if (RecipientType.user.name().equals(notification.getRecipientType())) {
            if (!StringUtils.hasText(notification.getRecipientLogin())) {
                log.warn("Yandex Messenger recipient login is missing notificationId={}, deliveryId={}",
                        notification.getId(),
                        delivery.getId());
                return Mono.just(ProviderSendResult.permanent(null, "missing_recipient", "Recipient login is required"));
            }
            request.put("login", notification.getRecipientLogin());
        } else if (RecipientType.chat.name().equals(notification.getRecipientType())) {
            if (!StringUtils.hasText(notification.getRecipientChatId())) {
                log.warn("Yandex Messenger recipient chat_id is missing notificationId={}, deliveryId={}",
                        notification.getId(),
                        delivery.getId());
                return Mono.just(ProviderSendResult.permanent(null, "missing_recipient", "Recipient chat_id is required"));
            }
            request.put("chat_id", notification.getRecipientChatId());
        } else {
            log.warn("Yandex Messenger recipient type is unsupported notificationId={}, deliveryId={}, recipientType={}",
                    notification.getId(),
                    delivery.getId(),
                    notification.getRecipientType());
            return Mono.just(ProviderSendResult.permanent(null, "invalid_recipient_type", "Unsupported recipient type"));
        }

        log.info("Calling Yandex Messenger notificationId={}, deliveryId={}, recipientType={}, payloadId={}",
                notification.getId(),
                delivery.getId(),
                notification.getRecipientType(),
                delivery.getProviderPayloadId());
        return webClientBuilder.baseUrl(config.getBaseUrl())
                .build()
                .post()
                .uri("/messages/sendText/")
                .header(HttpHeaders.AUTHORIZATION, "OAuth " + config.getOauthToken())
                .bodyValue(request)
                .retrieve()
                .bodyToMono(Map.class)
                .map(response -> {
                    var messageId = extractMessageId(response);
                    log.info("Yandex Messenger accepted notificationId={}, deliveryId={}, messageId={}",
                            notification.getId(),
                            delivery.getId(),
                            messageId);
                    return ProviderSendResult.success(messageId, "200");
                })
                .onErrorResume(WebClientResponseException.class, ex -> {
                    var status = ex.getStatusCode();
                    var retryable = isRetryable(status);
                    var message = StringUtils.hasText(ex.getResponseBodyAsString())
                            ? ex.getResponseBodyAsString()
                            : ex.getMessage();
                    log.warn("Yandex Messenger HTTP error notificationId={}, deliveryId={}, status={}, retryable={}",
                            notification.getId(),
                            delivery.getId(),
                            status.value(),
                            retryable);
                    return Mono.just(retryable
                            ? ProviderSendResult.retryable(Integer.toString(status.value()), "provider_http_error", message)
                            : ProviderSendResult.permanent(Integer.toString(status.value()), "provider_http_error", message));
                })
                .onErrorResume(WebClientRequestException.class, ex -> {
                    log.warn("Yandex Messenger request error notificationId={}, deliveryId={}, error={}",
                            notification.getId(),
                            delivery.getId(),
                            ex.getMessage());
                    return Mono.just(ProviderSendResult.retryable(null, "provider_request_error", ex.getMessage()));
                })
                .onErrorResume(ex -> {
                    log.warn("Unexpected Yandex Messenger send error notificationId={}, deliveryId={}",
                            notification.getId(),
                            delivery.getId(),
                            ex);
                    return Mono.just(ProviderSendResult.retryable(null, "provider_unexpected_error", ex.getMessage()));
                });
    }

    private boolean isRetryable(HttpStatusCode status) {
        return status.value() == 429 || status.is5xxServerError();
    }

    private String extractMessageId(Map<?, ?> response) {
        if (response == null) {
            return null;
        }
        var messageId = response.get("message_id");
        return messageId == null ? null : messageId.toString();
    }
}
