package ru.abondin.hreasy.platform.service.assessment.dto;

import lombok.Data;

import jakarta.validation.Valid;
import org.springframework.lang.NonNull;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Data
@Valid
public class CreateAssessmentDto {
    @NonNull
    private LocalDate plannedDate;

    @NonNull
    private List<Integer> managers = new ArrayList<>();
}
