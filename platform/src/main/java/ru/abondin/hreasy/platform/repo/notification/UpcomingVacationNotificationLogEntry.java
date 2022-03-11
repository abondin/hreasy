package ru.abondin.hreasy.platform.repo.notification;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * Log send notification event to avoid repeated delivery
 */
@Data
@Table("notify.upcoming_vacation_notification_log")
public class UpcomingVacationNotificationLogEntry {
    @Id
    private int id;

    private int vacation;

    private int employee;

    @NotNull
    private OffsetDateTime createdAt;

}
