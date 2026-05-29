package ru.abondin.hreasy.notifyms.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.abondin.hreasy.notifyms.config.NotificationProperties;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.ZoneId;

@Service
@RequiredArgsConstructor
public class BusinessHoursService {
    private final NotificationProperties props;

    public OffsetDateTime dueAt(String mode, OffsetDateTime now) {
        if (!"business-hours-immediate".equals(mode)) {
            return now;
        }
        var workingHours = props.getWorkingHours();
        var zone = ZoneId.of(workingHours.getTimezone());
        var zonedNow = now.atZoneSameInstant(zone);
        if (workingHours.getWorkdays().contains(zonedNow.getDayOfWeek())
                && !zonedNow.toLocalTime().isBefore(workingHours.getStart())
                && zonedNow.toLocalTime().isBefore(workingHours.getEnd())) {
            return now;
        }
        var nextDate = zonedNow.toLocalDate();
        if (!zonedNow.toLocalTime().isBefore(workingHours.getStart())) {
            nextDate = nextDate.plusDays(1);
        }
        nextDate = nextWorkday(nextDate);
        return nextDate.atTime(workingHours.getStart()).atZone(zone).toOffsetDateTime();
    }

    private LocalDate nextWorkday(LocalDate date) {
        var workingHours = props.getWorkingHours();
        var next = date;
        while (!workingHours.getWorkdays().contains(next.getDayOfWeek())) {
            next = next.plusDays(1);
        }
        return next;
    }
}
