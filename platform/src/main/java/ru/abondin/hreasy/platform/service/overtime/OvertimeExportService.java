package ru.abondin.hreasy.platform.service.overtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.EmployeeService;
import ru.abondin.hreasy.platform.service.dict.DictService;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class OvertimeExportService {

    private final EmployeeService employeeService;
    private final OvertimeService overtimeService;
    private final DictService dictService;
    private final DateTimeService dateTimeService;
    private final OvertimeSecurityValidator securityValidator;
    private final OvertimeReportExcelExporter excelExporter;

    public Mono<Resource> export(int periodId, AuthContext auth, Locale locale) {
        log.info("Export overtime summary for {} by {}", periodId, auth.getUsername());
        return securityValidator.validateExportOvertimes(auth).then(
                overtimeService.getSummary(periodId, auth).collectList().flatMap(
                        overtimes -> employeeService.findAll(auth, true).collectList().flatMap(
                                employees -> dictService.findProjects(auth).collectList().flatMap(projects -> export(
                                        OvertimeReportExcelExporter.OvertimeExportBundle.builder()
                                                .locale(locale)
                                                .overtimes(overtimes)
                                                .period(periodId)
                                                .employees(employees)
                                                .projects(projects)
                                                .exportTime(dateTimeService.now())
                                                .build()
                                )))
                )
        );
    }

    private Mono<Resource> export(OvertimeReportExcelExporter.OvertimeExportBundle bundle) {
        try (var out = new ByteArrayOutputStream()) {
            excelExporter.exportReportForPeriod(bundle, out);
            var resource = new ByteArrayResource(out.toByteArray());
            return Mono.just(resource);
        } catch (IOException e) {
            log.error("Unable to generate overtime report document", e);
            return Mono.error(new BusinessError("errors.export"));
        }
    }

}
