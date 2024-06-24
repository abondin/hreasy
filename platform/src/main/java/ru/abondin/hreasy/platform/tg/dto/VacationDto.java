package ru.abondin.hreasy.platform.tg.dto;

import java.time.LocalDate;

public record VacationDto(LocalDate startDate, LocalDate endDate) {
}
