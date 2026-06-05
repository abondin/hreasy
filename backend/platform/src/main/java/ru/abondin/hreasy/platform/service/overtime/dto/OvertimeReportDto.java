package ru.abondin.hreasy.platform.service.overtime.dto;

import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.Nullable;

import org.springframework.lang.NonNull;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@NoArgsConstructor
public class OvertimeReportDto {
    private Integer id;

    private int employeeId;

    /**
     * Overtime report period in yyyymm format.
     * For example 202005 for all overtimes, reported in June
     */
    @NonNull
    private int period;

    private List<OvertimeItemDto> items = new ArrayList<>();

    private List<OvertimeApprovalDecisionDto> approvals = new ArrayList<>();

    /**
     * Last created_at of items or null if there are no items in report
     *
     * @return
     */
    @Nullable
    public OffsetDateTime getLastUpdate() {
        return items.stream()
                .map(OvertimeItemDto::getCreatedAt)
                .max(OffsetDateTime::compareTo)
                .orElse(null);
    }
}
