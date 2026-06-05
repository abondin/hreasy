package ru.abondin.hreasy.platform.repo.notification;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import org.springframework.lang.NonNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Log send notification event to avoid repeated delivery
 */
@Data
@NoArgsConstructor
@Table("notify.upcoming_vacation_notification_log")
public class UpcomingVacationNotificationLogEntry {
    @Id
    private int id;

    @NonNull
    private int vacation;

    private int employee;

    @NonNull
    private OffsetDateTime createdAt;

    @NonNull
    private LocalDate vacationStartDate;

}
