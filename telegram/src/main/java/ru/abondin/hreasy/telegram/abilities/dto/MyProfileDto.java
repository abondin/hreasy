package ru.abondin.hreasy.telegram.abilities.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class MyProfileDto {
    private String displayName;
    private String projectName;
    private List<String> projectManagers = new ArrayList<>();
    private List<VacationDto> upcomingVacations = new ArrayList<>();

    @Data
    public static class VacationDto {
        private LocalDate startDate;
        private LocalDate endDate;
        @Override
        public String toString() {
            return formatDate(startDate) + " - " + formatDate(endDate);
        }

        private String formatDate(LocalDate date) {
            return Optional.ofNullable(date).map(LocalDate::toString).orElse("-");
        }
    }

}
