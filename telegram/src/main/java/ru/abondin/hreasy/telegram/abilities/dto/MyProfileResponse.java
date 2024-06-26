package ru.abondin.hreasy.telegram.abilities.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class MyProfileResponse {
    private String displayName;
    private String projectName;
    private List<String> projectManagers = new ArrayList<>();
    private List<VacationDto> upcomingVacations = new ArrayList<>();

}
