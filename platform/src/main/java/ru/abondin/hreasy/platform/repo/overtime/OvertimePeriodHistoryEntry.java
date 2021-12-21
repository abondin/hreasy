package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * Closed period to report overtime
 */
@Data
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
    @NotNull
    private int period;

    /**
     * Link to {@link ru.abondin.hreasy.platform.repo.employee.EmployeeRepo.EmployeeShortInfoEntry}
     */
    @NotNull
    private int updatedBy;

    @NotNull
    private OffsetDateTime updatedAt;

    /**
     * Optional close period reason
     */
    @Nullable
    private String comment;

}
