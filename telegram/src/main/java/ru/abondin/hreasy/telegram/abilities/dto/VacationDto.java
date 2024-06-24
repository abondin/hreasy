package ru.abondin.hreasy.telegram.abilities.dto;

import java.time.LocalDate;

public record VacationDto(LocalDate startDate, LocalDate endDate) {
}
