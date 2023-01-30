package ru.abondin.hreasy.platform.repo.ts;

import lombok.Data;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.time.OffsetDateTime;

@Data
@Table("ts.timesheet_record")
public class TimesheetRecordEntry {
    @Column
    private Integer id;
    private int employee;
    private int businessAccount;
    private Integer project;
    @NotNull
    private LocalDate date;
    private short hours;
    @NotNull
    private OffsetDateTime createdAt;
    private int createdBy;
    private OffsetDateTime deletedAt;
    private Integer deletedBy;
}
