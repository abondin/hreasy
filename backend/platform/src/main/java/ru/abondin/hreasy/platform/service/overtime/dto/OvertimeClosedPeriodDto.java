package ru.abondin.hreasy.platform.service.overtime.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;
import java.time.OffsetDateTime;

/**
 * Closed period to report overtime
 */
@Data
public class OvertimeClosedPeriodDto {
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

}
