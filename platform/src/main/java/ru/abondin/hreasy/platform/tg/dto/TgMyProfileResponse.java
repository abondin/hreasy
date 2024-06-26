package ru.abondin.hreasy.platform.tg.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TgMyProfileResponse {
    private Integer id;
    private String displayName;
    private Integer projectId;
    private String projectName;
    private List<String> projectManagers = new ArrayList<>();
    private List<VacationDto> upcomingVacations = new ArrayList<>();

}
