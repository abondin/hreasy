package ru.abondin.hreasy.platform.service.vacation;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.mapper.VacationDtoMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Locale;

/**
 * Export vacations
 */
@Component
@Slf4j
@RequiredArgsConstructor
public class VacationExportService {

    @RequiredArgsConstructor
    @Data
    public static class VacationExportFilter {
        private final List<Integer> years;
    }

    private final VacationExcelExporter exporter;
    private final VacationService vacationService;
    private final DateTimeService dateTimeService;
    private final VacationSecurityValidator securityValidator;
    private final VacationDtoMapper mapper;
    private final I18Helper i18n;


    public Mono<Resource> export(AuthContext auth, VacationExportFilter filter, Locale locale) {
        log.info("Export vacations for {} by {}", filter, auth.getUsername());
        var now = dateTimeService.now();
        var years = vacationService.yearsOrDefault(filter.getYears());
        return securityValidator.validateCanEditOvertimes(auth)
                .then(
                        vacationService.findAll(auth, new VacationService.VacationFilter(years))
                                .map(v -> mapper.toExportProjection(v, i18n, locale))
                                .collectList().flatMap(vacations -> export(VacationExcelExporter.VacationsExportBundle.builder()
                                        .locale(locale)
                                        .exportTime(now)
                                        .vacations(vacations)
                                        .years(years)
                                        .exportedBy(auth.getUsername())
                                        .build()))
                );
    }

    private Mono<Resource> export(VacationExcelExporter.VacationsExportBundle bundle) {
        try (var out = new ByteArrayOutputStream()) {
            exporter.exportVacations(bundle, out);
            var resource = new ByteArrayResource(out.toByteArray());
            return Mono.just(resource);
        } catch (IOException e) {
            log.error("Unable to generate vacations export document", e);
            return Mono.error(new BusinessError("errors.export"));
        }
    }
}
