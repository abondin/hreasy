package ru.abondin.hreasy.platform.repo.dict;

import io.r2dbc.postgresql.codec.Json;
import lombok.Builder;
import lombok.Data;
import org.springframework.data.relational.core.mapping.Table;

/**
 * Business days, weekend days and holidays
 */
@Table("dict.working_days_calendar")
@Data
public class DictWorkingDaysCalendarEntry {
    private int year;
    private String region;
    private String type;
    /**
     * @see ru.abondin.hreasy.platform.service.dict.dto.DictWorkingDaysCalendarDto
     */
    private Json calendar;

    @Data
    @Builder
    public static class WorkingDaysCalendarEntryPk {
        private int year;
        private String region;
        private String type;
    }
}
