package ru.abondin.hreasy.platform.service.assessment;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jxls.transform.poi.JxlsPoiTemplateFillerBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.assessment.dto.EmployeeAssessmentsSummary;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

/**
 * Export overtime report and save it to disk
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AssessmentsSummaryExcelExporter {

    private final I18Helper i18Helper;

    @Setter
    @Value("${classpath:jxls/assessments_summary_template.xlsx}")
    private Resource template;

    @Data
    @Builder
    public static class AssessmentsSummaryExportBundle {
        private List<EmployeeAssessmentsSummary> assessments;
        private OffsetDateTime exportTime;
        private Locale locale;
    }


    public void exportAssessmentsSummary(AssessmentsSummaryExportBundle bundle, OutputStream out) throws IOException {
        try (var is = template.getInputStream()) {
            var context = new HashMap<String, Object>();
            context.put("assessments", bundle.getAssessments());
            context.put("exportedAt", i18Helper.formatDateTime(bundle.getLocale(), bundle.getExportTime()));
            JxlsPoiTemplateFillerBuilder.newInstance().withTemplate(is).buildAndFill(context, () -> out);
        }
    }
}
