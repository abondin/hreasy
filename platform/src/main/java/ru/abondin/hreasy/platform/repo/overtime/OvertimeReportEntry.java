package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;

import javax.validation.constraints.NotNull;

@Data
@Table("overtime_report")
public class OvertimeReportEntry {
    /**
     * Undefined for new report
     */
    @Id
    private Integer id;

    private int employeeId;

    /**
     * Overtime report period in yyyymm format.
     * For example 202005 for all overtimes, reported in June
     */
    @NotNull
    private int period;
}
