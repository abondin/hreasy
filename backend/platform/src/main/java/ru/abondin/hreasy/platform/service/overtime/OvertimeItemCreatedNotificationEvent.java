package ru.abondin.hreasy.platform.service.overtime;

import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.notification.BusinessNotificationEvent;

import java.time.LocalDate;

/**
 * Event emitted after an overtime item has been added to a report.
 *
 * @param auth action initiator
 * @param employeeId overtime report employee
 * @param reportId overtime report id
 * @param period overtime report period
 * @param itemId overtime item id
 * @param itemDate overtime item date
 * @param hours overtime item hours
 */
public record OvertimeItemCreatedNotificationEvent(
        AuthContext auth,
        int employeeId,
        Integer reportId,
        int period,
        Integer itemId,
        LocalDate itemDate,
        float hours
) implements BusinessNotificationEvent {
}
