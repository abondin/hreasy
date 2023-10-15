package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;
import java.time.OffsetDateTime;

/**
 * Closed period to report overtime
 */
@Data
@NoArgsConstructor
@Table("ovt.overtime_period_history")
public class OvertimePeriodHistoryEntry {

    public static short ACTION_CLOSE = 1;
    public static short ACTION_REOPEN = 2;


    @Id
    private Integer id;

    private short action;

    /**
     * Overtime report period in yyyymm format.
     * For example 202005 for all overtimes, reported in June
     */
    @NonNull
    private int period;

    /**
     * Link to {@link ru.abondin.hreasy.platform.repo.employee.EmployeeRepo.EmployeeShortInfoEntry}
     */
    @NonNull
    private int updatedBy;

    @NonNull
    private OffsetDateTime updatedAt;

    /**
     * Optional close period reason
     */
    @Nullable
    private String comment;

}
