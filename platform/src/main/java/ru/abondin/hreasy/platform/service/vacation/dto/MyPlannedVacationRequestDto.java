package ru.abondin.hreasy.platform.service.vacation.dto;

import jakarta.validation.Valid;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.lang.NonNull;

import java.time.LocalDate;

@Data
@NoArgsConstructor
@Valid
public class MyPlannedVacationRequestDto {
    @NonNull
    private Integer year;
    @NonNull
    private Integer daysNumber;
    @NonNull
    private LocalDate startDate;
    @NonNull
    private LocalDate endDate;
    private String notes;
}
