package ru.abondin.hreasy.platform.repo.ts;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import org.springframework.lang.NonNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@Table("ts.timesheet_record")
public class TimesheetRecordEntry {
    @Id
    private Integer id;
    private Integer employee;
    private int businessAccount;
    private Integer project;
    private LocalDate date;
    private short hoursSpent;

    private String comment;

    private OffsetDateTime updatedAt;
    private int updatedBy;
}
