package ru.abondin.hreasy.platform.service.notification;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.notification.dto.NewNotificationDto;

import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class NotificationOrchestratorTest {

    @Test
    void actionLinkIsAddedOnlyToExternalNotificationBody() {
        var handler = Mockito.mock(TestNotificationHandler.class);
        var persistService = Mockito.mock(NotificationPersistService.class);
        var notifyMsClient = Mockito.mock(NotifyMsClient.class);
        var localeResolver = Mockito.mock(NotificationLocaleResolver.class);
        var i18n = new I18Helper.DummyI18Helper();

        when(handler.eventClass()).thenReturn(TestNotificationEvent.class);

        var orchestrator = new NotificationOrchestrator(
                List.of(handler),
                persistService,
                notifyMsClient,
                localeResolver,
                new ObjectMapper(),
                i18n);

        var plan = NotificationPlan.builder()
                .eventType("test.event")
                .category("test")
                .dedupeKey("test:1")
                .recipient(NotificationRecipient.user("user@example.com", 10))
                .priority("normal")
                .titleKey("notification.test.title")
                .bodyKey("notification.test.body")
                .actionTitleKey("notification.test.action")
                .actionUrl("https://hreasy.example.com/employees/10/changeCurrentProject")
                .context("employeeId", 10)
                .build();

        when(handler.build(any(TestNotificationEvent.class))).thenReturn(Flux.just(plan));
        when(localeResolver.resolve(any())).thenReturn(Locale.of("ru", "RU"));
        when(persistService.sendNotificationTo(any(NewNotificationDto.class), anyList(), eq(null)))
                .thenReturn(Flux.just(1));
        when(notifyMsClient.sendBestEffort(any())).thenReturn(Mono.empty());

        StepVerifier.create(orchestrator.publish(new TestNotificationEvent()))
                .verifyComplete();

        var inboxCaptor = ArgumentCaptor.forClass(NewNotificationDto.class);
        verify(persistService).sendNotificationTo(inboxCaptor.capture(), eq(List.of(10)), eq(null));
        assertEquals("notification.test.body", inboxCaptor.getValue().getMarkdownText());
        assertFalse(inboxCaptor.getValue().getMarkdownText().contains("https://hreasy.example.com"));

        var externalCaptor = ArgumentCaptor.forClass(NotifyMsClient.CreateNotificationRequest.class);
        verify(notifyMsClient).sendBestEffort(externalCaptor.capture());
        assertTrue(externalCaptor.getValue().body().contains("notification.test.body"));
        assertTrue(externalCaptor.getValue().body()
                .contains("[notification.test.action](https://hreasy.example.com/employees/10/changeCurrentProject)"));
    }

    private record TestNotificationEvent() implements BusinessNotificationEvent {
    }

    private interface TestNotificationHandler extends BusinessNotificationHandler<TestNotificationEvent> {
    }
}
