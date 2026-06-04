package ru.abondin.hreasy.platform.service.assessment;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.jxls.common.Context;
import org.jxls.util.JxlsHelper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.service.assessment.dto.EmployeeAssessmentsSummary;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Export overtime report and save it to disk
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class AssessmentsSummaryExcelExporter {

    // Uncomment me if any localization requires in exported data.
    // For example i18Helper.localize(bundle.locale, "enum.SomeStatus." + report.getStatusEnumValue().toString())
    //    private final I18Helper i18Helper;

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
            var context = new Context();
            context.putVar("assessments", bundle.getAssessments());
            context.putVar("exportedAt", bundle.getExportTime().toLocalDateTime());
            JxlsHelper.getInstance().processTemplate(is, out, context);
        }
    }
}
