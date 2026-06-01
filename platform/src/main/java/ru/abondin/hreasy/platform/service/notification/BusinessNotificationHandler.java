package ru.abondin.hreasy.platform.service.notification;

import reactor.core.publisher.Flux;

/**
 * Builds notification plans for one supported business event type.
 *
 * @param <E> business event type handled by this strategy
 */
public interface BusinessNotificationHandler<E extends BusinessNotificationEvent> {
    /**
     * Returns the exact event class supported by this handler.
     *
     * @return event class used by {@link NotificationOrchestrator} for handler lookup
     */
    Class<E> eventClass();

    /**
     * Builds one or more notification plans for the event.
     *
     * @param event business event with enough data to resolve recipients and templates
     * @return notification plans ready for common rendering and delivery orchestration
     */
    Flux<NotificationPlan> build(E event);
}
