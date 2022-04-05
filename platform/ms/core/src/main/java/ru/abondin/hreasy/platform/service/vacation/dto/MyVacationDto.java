package ru.abondin.hreasy.platform.service.vacation.dto;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.LocalDate;
import java.util.Arrays;

@Data
public class MyVacationDto {
    private int id;
    private int year;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;

    private VacationDto.VacationStatus status = VacationDto.VacationStatus.PLANNED;

    private String documents;

    private int daysNumber;

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;
}
