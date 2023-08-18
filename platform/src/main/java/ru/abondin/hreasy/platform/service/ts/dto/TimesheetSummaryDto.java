package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Builder;
import lombok.Data;

import javax.validation.constraints.NotNull;
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

    @Data
    public static class TimesheetShortForSummary {
        private int id;
        private LocalDate date;
        private String comment;
        private short hoursSpent;
    }

    @NotNull
    private EmployeeShortForTimesheetSummary employee;
    private Integer businessAccount;
    private Integer project;
    @NotNull
    private List<TimesheetShortForSummary> timesheet = new ArrayList<>();

    /**
     * Set of all days in vacations
     */
    private Set<LocalDate> vacationDays = new HashSet<>();
}
