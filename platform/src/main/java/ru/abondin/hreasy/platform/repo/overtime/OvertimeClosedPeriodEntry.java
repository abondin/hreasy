package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * Closed period to report overtime
 */
@Data
@Table("ovt.overtime_closed_period")
public class OvertimeClosedPeriodEntry implements Persistable<Integer> {
    /**
     * Overtime report period in yyyymm format.
     * For example 202005 for all overtimes, reported in June
     */
    @NotNull
    @Id
    private int period;

    /**
     * Link to {@link ru.abondin.hreasy.platform.repo.employee.EmployeeRepo.EmployeeShortInfoEntry}
     */
    @NotNull
    private int closedBy;

    @NotNull
    private OffsetDateTime closedAt;

    /**
     * Optional close period reason
     */
    @Nullable
    private String comment;


    /**
     * No update operation for this entity required
     *
     * @return
     */
    @Override
    public boolean isNew() {
        return true;
    }

    @Nullable
    @Override
    public Integer getId() {
        return period;
    }
}
