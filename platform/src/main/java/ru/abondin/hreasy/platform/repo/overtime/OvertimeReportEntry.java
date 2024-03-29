package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;
import java.time.OffsetDateTime;

@Data
@NoArgsConstructor
@Table("ovt.overtime_report")
public class OvertimeReportEntry {
    /**
     * Undefined for new report
     */
    @Id
    private Integer id;

    private int employee;

    /**
     * Overtime report period in yyyymm format.
     * For example 202005 for all overtimes, reported in June
     */
    @NonNull
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
