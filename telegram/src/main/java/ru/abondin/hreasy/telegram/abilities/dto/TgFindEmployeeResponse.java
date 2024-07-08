package ru.abondin.hreasy.telegram.abilities.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class TgFindEmployeeResponse {
    private List<EmployeeDto> employees = new ArrayList<>();
    private boolean hasMore = false;


    public record EmployeeDto(int id, String displayName, String email, String telegram, boolean telegramConfirmed, String officeLocation,
                              String projectName, String projectRole,
                              List<TgVacationDto> upcomingVacations, int score) {
    }

}
