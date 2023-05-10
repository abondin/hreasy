package ru.abondin.hreasy.platform.service.ts.dto;

import lombok.Data;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
public class TimesheetSummaryDto {
    @NotNull
    private int employee;
    private int businessAccount;
    private Integer project;
    @NotNull
    private LocalDate date;
    private short hoursSpent;
    private String description;
}
