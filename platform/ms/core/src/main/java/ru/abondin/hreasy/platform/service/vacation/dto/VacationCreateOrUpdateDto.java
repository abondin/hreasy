package ru.abondin.hreasy.platform.service.vacation.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;

@Data
@Valid
public class VacationCreateOrUpdateDto {
    @NotNull(message = "Year is required")
    private Integer year;
    @NotNull(message = "Start Date is required")
    private LocalDate startDate;
    @NotNull(message = "Ent Date is required")
    private LocalDate endDate;
    private String notes;
    @NotNull(message = "Status is required")
    private VacationDto.VacationStatus status = VacationDto.VacationStatus.PLANNED;
    private String documents;
    private Integer daysNumber;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
}
