package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyCommonProps;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.ImportEmployeesWorkflowRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.dto.ImportEmployeeExcelDto;
import ru.abondin.hreasy.platform.service.admin.employee.dto.ImportEmployeesWorkflowDto;
import ru.abondin.hreasy.platform.service.dict.DictService;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminEmployeeImportService {
    private final AdminEmployeeService employeeService;
    private final DictService dictService;
    private final AdminEmployeeExcelImporter exporter;
    private final ImportEmployeesWorkflowRepo workflowRepo;
    private final DateTimeService dateTimeService;
    private final EmployeeWithAllDetailsRepo emplRepo;

    private final HrEasyCommonProps props;

    private final I18Helper i18n;

    private DateTimeFormatter formatter;


    @PostConstruct
    public void postConstruct() {
        this.formatter = DateTimeFormatter.ofPattern(props.getImportEmployee().getDateFormat());
    }

    public Mono<ImportEmployeesWorkflowDto> startImportProcess(AuthContext auth, EmployeeImportConfig config, Resource inputFile) {
        log.info("Start import employee process by {}", auth.getUsername());
        final OffsetDateTime now = dateTimeService.now();
        try {
            return
                    // 1. Parse the Excel
                    exporter.importEmployees(config, inputFile.getInputStream())
                            .collectList()
                            .flatMap(fromExcel -> {
                                // 2. Load required dictionaries
                                return Mono.zip(dictService.findPositions(auth)
                                                .collectList(), dictService.findDepartments(auth).collectList())
                                        .flatMap(dicts -> {
                                            // 3. Load employees
                                            return emplRepo.findByEmailsInLowerCase(
                                                    fromExcel.stream().map(
                                                                    e -> e.getEmail().trim().toLowerCase(Locale.getDefault()))
                                                            .collect(Collectors.toSet())
                                            ).collectList().flatMap(employees ->
                                                    // 4. Merge excel with existing employees and find the diff
                                                    merge(fromExcel, new ImportContext(employees, dicts.getT1(), dicts.getT2())));
                                        });
                            });
        } catch (IOException e) {
            log.error("Unable to read excel file", e);
            return Mono.error(new BusinessError("errors.import.unexpectedError"));
        }
    }

    private Mono<ImportEmployeesWorkflowDto> merge(List<ImportEmployeeExcelDto> fromExcel,
                                                   ImportContext context) {
        var result = new ImportEmployeesWorkflowDto();
        for (var excelRow : fromExcel) {
            parseRawData(excelRow, context);
        }
        result.setData(fromExcel);
        return Mono.just(result);
    }

    private void parseRawData(ImportEmployeeExcelDto excelRow, ImportContext context) {
        var existingEmpl = context.employees.stream().filter(e -> e.getEmail().trim().toLowerCase(Locale.getDefault())
                .equals(excelRow.getEmail().toLowerCase(Locale.getDefault()).trim())).findFirst();

        apply(excelRow.getBirthday(), existingEmpl, EmployeeWithAllDetailsEntry::getBirthday, context, this::applyLocalDate);
        apply(excelRow.getDepartment(), existingEmpl, EmployeeWithAllDetailsEntry::getDepartmentId, context, (p, c) -> applyDict(p, c.departments));
        apply(excelRow.getPosition(), existingEmpl, EmployeeWithAllDetailsEntry::getPositionId, context, (p, c) -> applyDict(p, c.positions));
        apply(excelRow.getDisplayName(), existingEmpl, EmployeeWithAllDetailsEntry::getDisplayName, context, this::applyStringWithTrim);

        apply(excelRow.getDisplayName(), existingEmpl, EmployeeWithAllDetailsEntry::getDisplayName, context, this::applyStringWithTrim);
        apply(excelRow.getDateOfDismissal(), existingEmpl, EmployeeWithAllDetailsEntry::getDateOfDismissal, context, this::applyLocalDate);
        apply(excelRow.getDateOfEmployment(), existingEmpl, EmployeeWithAllDetailsEntry::getDateOfEmployment, context, this::applyLocalDate);
        apply(excelRow.getDocumentIssuedDate(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentIssuedDate, context, this::applyLocalDate);
        apply(excelRow.getDocumentSeries(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentSeries, context, this::applyStringWithTrim);
        apply(excelRow.getDocumentNumber(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentNumber, context, this::applyStringWithTrim);
        apply(excelRow.getExternalErpId(), existingEmpl, EmployeeWithAllDetailsEntry::getExtErpId, context, this::applyStringWithTrim);
        apply(excelRow.getRegistrationAddress(), existingEmpl, EmployeeWithAllDetailsEntry::getRegistrationAddress, context, this::applyStringWithTrim);
        // TODO Set phone datatype in database to string
        // apply(excelRow.getPhone(), existingEmpl, EmployeeWithAllDetailsEntry::getPhone, context, this::applyStringWithTrim);
        apply(excelRow.getSex(), existingEmpl, EmployeeWithAllDetailsEntry::getSex, context, this::applySex);
        apply(excelRow.getDisplayName(), existingEmpl, EmployeeWithAllDetailsEntry::getDisplayName, context, this::applyStringWithTrim);


    }


    private <T> void apply(ImportEmployeeExcelDto.DataProperty<T> prop,
                           Optional<EmployeeWithAllDetailsEntry> existingEmployee,
                           Function<EmployeeWithAllDetailsEntry, T> existingEmployeeProp,
                           ImportContext context,
                           BiConsumer<ImportEmployeeExcelDto.DataProperty<T>, ImportContext> mapper) {
        prop.setCurrentValue(existingEmployee.map(existingEmployeeProp).orElse(null));
        mapper.accept(prop, context);
    }

    private void applyDict(ImportEmployeeExcelDto.DataProperty<Integer> prop, List<SimpleDictDto> dict) {
        if (prop.getRaw() != null) {
            var value = prop.getRaw().trim().toLowerCase(Locale.getDefault());
            var key = dict
                    .stream()
                    .filter(d -> d.getName().toLowerCase(Locale.getDefault()).trim().equals(value))
                    .findFirst();
            if (key.isPresent()) {
                prop.setImportedValue(key.get().getId());
            } else {
                prop.setError(i18n.localize("errors.import.not_in_dict"));
            }
        }
    }

    private void applyLocalDate(ImportEmployeeExcelDto.DataProperty<LocalDate> prop, ImportContext ctx) {
        if (prop.getRaw() != null) {
            try {
                var date = formatter.parse(prop.getRaw().trim(), LocalDate::from);
                prop.setImportedValue(date);
            } catch (DateTimeParseException ex) {
                prop.setError(i18n.localize("errors.import.invalid_date_format", props.getImportEmployee().getDateFormat()));
            }
        }
    }

    private void applyStringWithTrim(ImportEmployeeExcelDto.DataProperty<String> prop, ImportContext ctx) {
        prop.setImportedValue(prop.getImportedValue() == null ? null : prop.getImportedValue().trim());
    }

    private void applySex(ImportEmployeeExcelDto.DataProperty<String> prop, ImportContext ctx) {

    }


    @Data
    @RequiredArgsConstructor
    private static class ImportContext {
        private final List<EmployeeWithAllDetailsEntry> employees;
        private final List<SimpleDictDto> positions;
        private final List<SimpleDictDto> departments;
    }

}
