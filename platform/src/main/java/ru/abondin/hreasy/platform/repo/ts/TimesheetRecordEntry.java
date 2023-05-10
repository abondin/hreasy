package ru.abondin.hreasy.platform.repo.ts;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Table("ts.timesheet_record")
public class TimesheetRecordEntry {
    @Id
    private Integer id;
    private Integer employee;
    private int businessAccount;
    private Integer project;
    @NotNull
    private LocalDate date;
    private short hoursSpent;

    private String comment;

    @NotNull
    private OffsetDateTime createdAt;
    private int createdBy;
    private OffsetDateTime deletedAt;
    private Integer deletedBy;
}
