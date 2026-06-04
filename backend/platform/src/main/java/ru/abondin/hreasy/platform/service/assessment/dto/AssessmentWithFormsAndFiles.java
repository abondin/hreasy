package ru.abondin.hreasy.platform.service.assessment.dto;

import lombok.Data;
import org.springframework.lang.Nullable;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
public class AssessmentWithFormsAndFiles extends AssessmentDto {
    @Data
    public static class AssessmentFormDto {
        private int id;
        private int owner;
        private int formType;
        @Nullable
        private String content;
        @Nullable
        private OffsetDateTime completedAt;
        @Nullable
        private Integer completedBy;
    }

    private List<AssessmentFormDto> forms = new ArrayList<>();
    private List<String> attachmentsFilenames = new ArrayList<>();
    private String attachmentsAccessToken;
}
