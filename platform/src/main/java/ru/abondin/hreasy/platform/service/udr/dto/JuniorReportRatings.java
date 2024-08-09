package ru.abondin.hreasy.platform.service.udr.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class JuniorReportRatings {
    private int competence = 1;
    private int process = 1;
    private int teamwork = 1;
    private int contribution = 1;
    private int motivation = 1;
}
