package ru.abondin.hreasy.telegram.abilities.dto;

import java.time.LocalDate;

public record TgVacationDto(LocalDate startDate, LocalDate endDate) {
}
