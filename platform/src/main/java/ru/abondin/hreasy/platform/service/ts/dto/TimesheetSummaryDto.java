package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Builder;
import lombok.Data;

import org.springframework.lang.NonNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Data
public class TimesheetSummaryDto {

    @Data
    @Builder
    public static class EmployeeShortForTimesheetSummary {
        private int id;
        private String displayName;
        private Integer currentProject;
        private Integer currentProjectBa;
    }


    @NonNull
    private EmployeeShortForTimesheetSummary employee;
    @NonNull
    private List<TimesheetDto> timesheet = new ArrayList<>();

    /**
     * Set of all days in vacations
     */
    private Set<LocalDate> vacationDays = new HashSet<>();
}
