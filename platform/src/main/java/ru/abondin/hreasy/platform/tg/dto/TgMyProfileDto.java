package ru.abondin.hreasy.platform.tg.dto;

import lombok.Data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Data
public class TgMyProfileDto {
    private Integer id;
    private String displayName;
    private Integer projectId;
    private String projectName;
    private List<String> projectManagers = new ArrayList<>();
    private List<VacationDto> upcomingVacations = new ArrayList<>();

    public record VacationDto(LocalDate startDate, LocalDate endDate) {
    }
}
