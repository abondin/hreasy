package ru.abondin.hreasy.platform.service.assessment.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class DeleteAssessmentAttachmentResponse {
    private boolean deleted = true;
}
