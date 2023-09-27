package ru.abondin.hreasy.platform.service.salary.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.lang.Nullable;

import javax.validation.constraints.NotNull;
import java.time.OffsetDateTime;

/**
 * Closed period to report salary request
 */
@Data
public class SalaryRequestClosedPeriodDto {
    /**
     * salary request report period in yyyymm format.
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

}
