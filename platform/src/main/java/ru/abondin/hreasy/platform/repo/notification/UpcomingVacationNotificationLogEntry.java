package ru.abondin.hreasy.platform.repo.notification;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Log send notification event to avoid repeated delivery
 */
@Data
@Table("notify.upcoming_vacation_notification_log")
public class UpcomingVacationNotificationLogEntry {
    @Id
    private int id;

    @NotNull
    private int vacation;

    private int employee;

    @NotNull
    private OffsetDateTime createdAt;

    @NotNull
    private LocalDate vacationStartDate;

}
