package ru.abondin.hreasy.telegram.abilities.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TgMyProfileResponse {
    private String displayName;
    private String projectName;
    private List<String> projectManagers = new ArrayList<>();
    private List<TgVacationDto> upcomingVacations = new ArrayList<>();

}
