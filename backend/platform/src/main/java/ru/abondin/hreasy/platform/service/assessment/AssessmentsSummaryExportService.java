package ru.abondin.hreasy.platform.service.assessment;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.DateTimeService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AssessmentsSummaryExportService {

    private final AssessmentService assessmentService;
    private final DateTimeService dateTimeService;
    private final AssessmentSecurityValidator securityValidator;
    private final AssessmentsSummaryExcelExporter excelExporter;

    public Mono<Resource> export(AuthContext auth, Locale locale) {
        log.info("Export assessments summary for {} by {}", auth.getUsername());
        return securityValidator.validateExportAssessments(auth).then(
                assessmentService.allNotFiredEmployeesWithLatestAssessment(auth).collectList().flatMap(assessments ->
                        export(
                                AssessmentsSummaryExcelExporter.AssessmentsSummaryExportBundle.builder()
                                        .locale(locale)
                                        .assessments(assessments)
                                        .exportTime(dateTimeService.now())
                                        .build()
                        )
                )
        );
    }

    private Mono<Resource> export(AssessmentsSummaryExcelExporter.AssessmentsSummaryExportBundle bundle) {
        try (var out = new ByteArrayOutputStream()) {
            excelExporter.exportAssessmentsSummary(bundle, out);
            var resource = new ByteArrayResource(out.toByteArray());
            return Mono.just(resource);
        } catch (IOException e) {
            log.error("Unable to generate assessments summary document", e);
            return Mono.error(new BusinessError("errors.export"));
        }
    }

}
