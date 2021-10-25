package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.DictService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeAllFieldsMapper;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.Locale;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEmployeeExportService {
    private final AdminEmployeeService employeeService;
    private final DictService dictService;
    private final AdminEmployeeExcelExporter exporter;
    private final EmployeeAllFieldsMapper mapper;
    private final DateTimeService dateTimeService;

    public Mono<Resource> export(AuthContext auth, Locale locale) {
        final OffsetDateTime now = dateTimeService.now();
        return employeeService.findAll(auth)
                .map(e -> {
                    //TODO Populate dictionaries
                    var emplExp = mapper.toExportWithoutDictanories(e);
                    emplExp.setPosition(null);
                    emplExp.setLevel(null);
                    emplExp.setOfficeLocation(null);
                    emplExp.setDepartment(null);
                    emplExp.setCurrentProject(null);
                    return emplExp;
                }).collectList().map(emplList ->
                        AdminEmployeeExcelExporter.AdminEmployeeExportBundle.builder()
                                .exportTime(now)
                                .exportedBy(auth.getUsername())
                                .locale(locale)
                                .employees(emplList)
                                .build()
                )
                .flatMap(bundle -> export(bundle));
    }


    private Mono<Resource> export(AdminEmployeeExcelExporter.AdminEmployeeExportBundle bundle) {
        try (var out = new ByteArrayOutputStream()) {
            exporter.exportEmployees(bundle, out);
            var resource = new ByteArrayResource(out.toByteArray());
            return Mono.just(resource);
        } catch (IOException e) {
            log.error("Unable to generate employees export report document", e);
            return Mono.error(new BusinessError("errors.export"));
        }
    }
}
