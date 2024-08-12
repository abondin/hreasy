package ru.abondin.hreasy.platform.service.udr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.udr.UdrProps;
import ru.abondin.hreasy.platform.repo.udr.JuniorRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.udr.dto.JuniorDto;
import ru.abondin.hreasy.platform.service.udr.dto.JuniorRegistryMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminJuniorRegistryExportService {
    private final DateTimeService dateTimeService;
    private final JuniorSecurityValidator securityValidator;
    private final JuniorRepo juniorRepo;
    private final JuniorRegistryMapper mapper;
    private final AdminJuniorRegistryExcelExporter exporter;
    private final UdrProps props;


    @Transactional(readOnly = true)
    public Mono<Resource> export(AuthContext auth, Locale locale, JuniorExportFilter filter) {
        log.info("Export all juniors by {}. includeGraduated: {}", auth.getUsername(), filter);
        return securityValidator.export(auth).flatMap(v -> fetchJuniors(auth, filter)
                .map(mapper::toExportDto).collectList().flatMap(items -> export(
                        AdminJuniorRegistryExcelExporter.AdminJuniorRegistryExportBundle.builder()
                                .exportedBy(auth.getUsername())
                                .exportedAt(dateTimeService.now())
                                .locale(locale)
                                .items(items)
                                .build()
                )));
    }


    private Flux<JuniorDto> fetchJuniors(AuthContext auth, JuniorExportFilter filter) {
        var now = dateTimeService.now();
        return juniorRepo.findForExport(dateTimeService.now(),
                        auth.getEmployeeInfo().getEmployeeId(),
                        filter.includeGraduated)
                .map(e -> mapper.toDto(e, now, props));
    }

    private Mono<Resource> export(AdminJuniorRegistryExcelExporter.AdminJuniorRegistryExportBundle bundle) {
        try (var out = new ByteArrayOutputStream()) {
            exporter.exportJuniors(bundle, out);
            var resource = new ByteArrayResource(out.toByteArray());
            return Mono.just(resource);
        } catch (IOException e) {
            log.error("Unable to generate junior registry document", e);
            return Mono.error(new BusinessError("errors.export"));
        }
    }

    public record JuniorExportFilter(boolean includeGraduated) {
    }

}
