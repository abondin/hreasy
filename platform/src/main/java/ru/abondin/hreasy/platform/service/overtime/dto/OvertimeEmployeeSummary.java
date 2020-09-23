package ru.abondin.hreasy.platform.service.overtime.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

/**
 * Aggregated overtime information
 */
@Data
public class OvertimeEmployeeSummary {

    /**
     * Overtimes for one employee, aggregated for given day and project
     */
    @Data
    public static class OvertimeDaySummary {
        private LocalDate date;
        private int projectId;
        private float hours;
    }

    private int employeeId;

    private List<OvertimeDaySummary> items = new ArrayList<>();
}
