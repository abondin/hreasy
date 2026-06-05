package ru.abondin.hreasy.platform.service.salary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSalaryRequestExportService {
    private final DateTimeService dateTimeService;
    private final SalarySecurityValidator securityValidator;
    private final AdminSalaryRequestService service;
    private final SalaryRequestMapper mapper;
    private final AdminSalaryRequestExcelExporter exporter;

    public Mono<Resource> export(int periodId, AuthContext auth, Locale locale) {
        log.info("Export all salary requests for period {} by {}", periodId, auth.getUsername());
        return securityValidator.validateExport(auth).flatMap(v -> service.findAll(auth, periodId)
                .map(mapper::toExportDto).collectList().flatMap(requests -> export(
                        AdminSalaryRequestExcelExporter.AdminSalaryRequestExportBundle.builder()
                                .exportedBy(auth.getUsername())
                                .exportTime(dateTimeService.now())
                                .period(periodId)
                                .locale(locale)
                                .requests(requests)
                                .build()
                )));
    }

    private Mono<Resource> export(AdminSalaryRequestExcelExporter.AdminSalaryRequestExportBundle bundle) {
        try (var out = new ByteArrayOutputStream()) {
            exporter.exportSalaryRequests(bundle, out);
            var resource = new ByteArrayResource(out.toByteArray());
            return Mono.just(resource);
        } catch (IOException e) {
            log.error("Unable to generate salary request report document", e);
            return Mono.error(new BusinessError("errors.export"));
        }
    }

}
