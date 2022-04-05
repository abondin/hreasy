package ru.abondin.hreasy.platform.service.assessment.dto;

import lombok.Data;

import javax.validation.Valid;
import javax.validation.constraints.NotNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Valid
public class CreateAssessmentDto {
    @NotNull
    private LocalDate plannedDate;

    @NotNull
    private List<Integer> managers = new ArrayList<>();
}
