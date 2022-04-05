package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Single item of overtime report
 *
 * @see OvertimeReportEntry
 */
@Data
@Table("ovt.overtime_item")
public class OvertimeItemEntry {
    @Id
    private Integer id;

    private Integer reportId;

    /**
     * YYYY-MM-DD format
     */
    private LocalDate date;
    private int projectId;
    private float hours;
    private String notes;

    private OffsetDateTime createdAt;
    private Integer createdBy;
    private OffsetDateTime deletedAt;
    private Integer deletedBy;

}
