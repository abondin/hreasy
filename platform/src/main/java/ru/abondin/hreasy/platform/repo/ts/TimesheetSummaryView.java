package ru.abondin.hreasy.platform.repo.ts;

import lombok.Data;

@Data
public class TimesheetSummaryView extends TimesheetRecordEntry{
    private String employeeDisplayName;
}
