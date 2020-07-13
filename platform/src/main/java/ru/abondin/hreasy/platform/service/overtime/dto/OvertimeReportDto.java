package ru.abondin.hreasy.platform.service.overtime.dto;

import lombok.Data;

import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
public class OvertimeReportDto {
    private Integer id;

    private int employeeId;

    /**
     * Overtime report period in yyyymm format.
     * For example 202005 for all overtimes, reported in June
     */
    @NotNull
    private int period;

    private List<OvertimeItemDto> items = new ArrayList<>();
}
