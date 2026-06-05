package ru.abondin.hreasy.platform.service.vacation.dto;

import lombok.Data;

import java.time.OffsetDateTime;

@Data
public class VacPlanningPeriodDto {
    private int year;
    private OffsetDateTime openedAt;
    private Integer openedBy;
    private OffsetDateTime closedAt;
    private Integer closedBy;
    private String comment;

}
