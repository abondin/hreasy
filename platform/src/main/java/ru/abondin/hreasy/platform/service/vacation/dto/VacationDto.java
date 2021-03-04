package ru.abondin.hreasy.platform.service.vacation.dto;

import lombok.Data;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.time.LocalDate;
import java.util.Arrays;

@Data
public class VacationDto {
    private int id;
    private int employee;
    private String employeeDisplayName;
    private SimpleDictDto employeeCurrentProject;
    private int year;
    private LocalDate startDate;
    private LocalDate endDate;
    private String notes;

    private VacationStatus status = VacationStatus.PLANNED;

    private String documents;

    private int daysNumber;

    private LocalDate plannedStartDate;
    private LocalDate plannedEndDate;

    public boolean isCanceled() {
        return this.status == VacationStatus.CANCELED;
    }

    @RequiredArgsConstructor
    @Getter
    public enum VacationStatus {
        PLANNED(0),
        TAKEN(1),
        COMPENSATION(2),
        CANCELED(3);

        private final int statusId;

        public static VacationStatus fromId(int statusId) {
            return Arrays.stream(values()).filter(s -> s.statusId == statusId).findFirst()
                    .orElseThrow(() -> new BusinessError("unsupported.vacation.status", Integer.toString(statusId)));
        }
    }
}
