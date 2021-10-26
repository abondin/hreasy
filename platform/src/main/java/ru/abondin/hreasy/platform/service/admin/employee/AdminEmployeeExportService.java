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
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

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
        return Mono.zip(
                dictService.findProjects(auth).collectList()
                , dictService.findDepartments(auth).collectList()
                , dictService.findLevels(auth).collectList()
                , dictService.findOfficeLocations(auth).collectList()
                , dictService.findPositions(auth).collectList()
        ).flatMap(tupple -> {
            var projects = fromDicts(tupple.getT1());
            var departments = fromDicts(tupple.getT2());
            var levels = fromDicts(tupple.getT3());
            var officeLocations = fromDicts(tupple.getT4());
            var positions = fromDicts(tupple.getT5());
            return employeeService.findAll(auth)
                    .map(e -> {
                        var emplExp = mapper.toExportWithoutDictanories(e);
                        emplExp.setCurrentProject(projects.get(e.getCurrentProjectId()));
                        emplExp.setDepartment(departments.get(e.getDepartmentId()));
                        emplExp.setPosition(positions.get(e.getPositionId()));
                        emplExp.setLevel(levels.get(e.getLevelId()));
                        emplExp.setOfficeLocation(officeLocations.get(e.getOfficeLocationId()));
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
        });
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

    private Map<Integer, String> fromDicts(List<SimpleDictDto> list) {
        return list.stream().collect(Collectors.toMap(SimpleDictDto::getId, SimpleDictDto::getName));
    }
}
