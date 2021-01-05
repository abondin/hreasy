package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;

import java.time.LocalDate;

/**
 * Sum of hours of all not deleted items for given reportId grouped by date and project
 */
@Data
public class OvertimeItemsGroupedByDateAndProjectView {
    private int reportId;
    private LocalDate date;
    private int projectId;
    private float hours;
}
