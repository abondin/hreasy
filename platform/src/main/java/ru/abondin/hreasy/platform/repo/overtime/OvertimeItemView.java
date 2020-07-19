package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;

import java.time.LocalDate;

/**
 * View of {@link OvertimeItemEntry} with information from {@link OvertimeReportEntry}
 */
@Data
public class OvertimeItemView {
    private Integer id;

    private Integer reportId;
    private Integer reportEmployeeId;
    private Integer reportPeriod;

    private LocalDate date;
    private int projectId;
    private int hours;
}
