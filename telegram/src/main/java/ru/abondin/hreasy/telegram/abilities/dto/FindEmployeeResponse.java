package ru.abondin.hreasy.telegram.abilities.dto;

import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
public class FindEmployeeResponse {
    private List<EmployeeDto> employees = new ArrayList<>();


    public record EmployeeDto(int id, String displayName, String email, String telegram, String officeLocation,
                              String projectName, String projectRole,
                              List<VacationDto> upcomingVacations, int score) {
    }

}
