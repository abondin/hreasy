package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

@Data
@Table("ovt.overtime_report")
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


    /**
     * Overtime report with summary
     *
     * @see OvertimeReportRepo#summary(int)
     */
    @Data
    public static class OvertimeReportSummaryEntry extends OvertimeReportEntry {
        @Nullable
        private float totalHours;

        @Nullable
        private OffsetDateTime lastUpdate;
        @Nullable
        private OffsetDateTime lastApprove;
        @Nullable
        private OffsetDateTime lastDecline;

    }
}
