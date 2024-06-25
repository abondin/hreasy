package ru.abondin.hreasy.platform.tg.dto;

import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Data
@AllArgsConstructor
public class FindEmployeeResponse {
    private List<EmployeeDto> employees = new ArrayList<>();
    private boolean hasMore = false;


    @Getter
    @RequiredArgsConstructor
    public static final class EmployeeDto {
        private final int id;
        private final String displayName;
        private final String email;
        private final String telegram;
        private final String officeLocation;
        private final Integer projectId;
        private final String projectName;
        private final String projectRole;
        @Setter
        private List<VacationDto> upcomingVacations;
        @Setter
        private int score;
    }

}
