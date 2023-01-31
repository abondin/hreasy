package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class TimesheetAggregatedRecordsDto {
    @NotNull
    private SimpleDictDto employee;
    private List<TimesheetAggregatedRecordItem> records = new ArrayList<>();

    @Data
    public static class TimesheetAggregatedRecordItem {
        @NotNull
        private LocalDate date;
        private short hours;
        private int businessAccount;
        private Integer project;
    }
}
