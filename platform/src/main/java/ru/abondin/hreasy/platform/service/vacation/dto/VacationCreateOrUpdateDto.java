package ru.abondin.hreasy.platform.service.vacation.dto;

import lombok.Data;

import jakarta.validation.Valid;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;
import java.time.LocalDate;

@Data
@NoArgsConstructor
@Valid
public class VacationCreateOrUpdateDto {
    @NonNull
    private Integer year;
    @NonNull
    private LocalDate startDate;
    @NonNull
    private LocalDate endDate;
    private String notes;
    @NonNull
    private VacationDto.VacationStatus status = VacationDto.VacationStatus.PLANNED;
    private String documents;
    private Integer daysNumber;
    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
}
