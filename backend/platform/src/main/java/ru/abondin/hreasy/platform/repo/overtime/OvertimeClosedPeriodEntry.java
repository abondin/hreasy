package ru.abondin.hreasy.platform.repo.overtime;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.domain.Persistable;
import org.springframework.data.relational.core.mapping.Table;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;
import java.time.OffsetDateTime;

/**
 * Closed period to report overtime
 */
@Data
@NoArgsConstructor
@Table("ovt.overtime_closed_period")
public class OvertimeClosedPeriodEntry implements Persistable<Integer> {
    /**
     * Overtime report period in yyyymm format.
     * For example 202005 for all overtimes, reported in June
     */
    @NonNull
    @Id
    private int period;

    /**
     * Link to {@link ru.abondin.hreasy.platform.repo.employee.EmployeeRepo.EmployeeShortInfoEntry}
     */
    @NonNull
    private int closedBy;

    @NonNull
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
