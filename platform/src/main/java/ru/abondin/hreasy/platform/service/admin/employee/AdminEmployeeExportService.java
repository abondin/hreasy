package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeAllFieldsMapper;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeExportFilter;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeWithAllDetailsDto;
import ru.abondin.hreasy.platform.service.ba.BusinessAccountService;
import ru.abondin.hreasy.platform.service.dict.DictService;
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
    private final BusinessAccountService baService;
    private final AdminEmployeeExcelExporter exporter;
    private final EmployeeAllFieldsMapper mapper;
    private final DateTimeService dateTimeService;


    public Mono<Resource> export(AuthContext auth, EmployeeExportFilter filter, Locale locale) {
        log.info("Export all employees by {}", auth.getUsername());
        final OffsetDateTime now = dateTimeService.now();
        // 1. Load all dictionaries
        return Mono.zip(
                dictService.findProjects(auth).collectList()
                , baService.findAllAsSimpleDict(false).collectList()
                , dictService.findDepartments(auth).collectList()
                , dictService.findLevels(auth).collectList()
                , dictService.findOfficeLocations(auth).collectList()
                , dictService.findPositions(auth).collectList()
                , dictService.findOrganizations(auth).collectList()
        ).flatMap(tupple -> {
            var projects = fromDicts(tupple.getT1());
            var bas = fromDicts(tupple.getT2());
            var departments = fromDicts(tupple.getT3());
            var levels = fromDicts(tupple.getT4());
            var officeLocations = fromDicts(tupple.getT5());
            var positions = fromDicts(tupple.getT6());
            var organizations = fromDicts(tupple.getT7());
            // 2. Load all employees (include fired)
            return employeeService.findAll(auth, filter.isIncludeFired())
                    .map(e -> {
                        var emplExp = mapper.toExportWithoutDictionaries(e);
                        emplExp.setCurrentProject(e.getCurrentProjectId() == null ? null : projects.get(e.getCurrentProjectId()));
                        emplExp.setCurrentProjectRole(e.getCurrentProjectRole());
                        emplExp.setDepartment(departments.get(e.getDepartmentId()));
                        emplExp.setOrganization(organizations.get(e.getOrganizationId()));
                        emplExp.setBa(bas.get(e.getBaId()));
                        emplExp.setPosition(positions.get(e.getPositionId()));
                        emplExp.setLevel(levels.get(e.getLevelId()));
                        emplExp.setOfficeLocation(officeLocations.get(e.getOfficeLocationId()));
                        return emplExp;
                    }).collectList().map(emplList ->
                            // 4. Prepare export bundle //TODO Get rid of collectList() and export employees in pipe
                            AdminEmployeeExcelExporter.AdminEmployeeExportBundle.builder()
                                    .exportTime(now)
                                    .exportedBy(auth.getUsername())
                                    .locale(locale)
                                    .employees(emplList)
                                    .build()
                    )
                    // 5. Write exported document to resource. //TODO Another place to migrate to pipes
                    .flatMap(this::export);
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

    private Map<Integer, String> fromDicts(List<? extends SimpleDictDto> list) {
        return list.stream().filter(d -> StringUtils.isNotBlank(d.getName()))
                .collect(Collectors.toMap(SimpleDictDto::getId, SimpleDictDto::getName));
    }
}
