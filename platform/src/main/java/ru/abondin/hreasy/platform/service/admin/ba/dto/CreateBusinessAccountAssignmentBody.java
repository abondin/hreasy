package ru.abondin.hreasy.platform.service.admin.ba.dto;

import lombok.Data;
import lombok.ToString;
import org.springframework.lang.Nullable;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Create new business account assignment from scratch
 */
@Data
@ToString(of = {"employee", "project", "baPosition"})
public class CreateBusinessAccountAssignmentBody {
    @Nullable
    private Integer employee;
    @Nullable
    private Integer project;
    @Nullable
    private String baPosition;
    @Nullable
    private BigDecimal employmentRate;
    @Nullable
    private BigDecimal employmentRateFactor = BigDecimal.ONE;
    @Nullable
    private BigDecimal baPositionRate;
    @Nullable
    private BigDecimal baPositionRateFactor = BigDecimal.ONE;
    @Nullable
    private String comment;
    @Nullable
    private LocalDate startDate;
    @Nullable
    private LocalDate endDate;
}
