package ru.abondin.hreasy.platform.service.udr.dto;

import com.fasterxml.jackson.annotation.JsonCreator;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor(onConstructor_ = @JsonCreator)
@AllArgsConstructor
public class JuniorReportRatings {
    private int overallReadiness = 0;
    private int competence = 0;
    private int process = 0;
    private int teamwork = 0;
    private int contribution = 0;
    private int motivation = 0;
}
