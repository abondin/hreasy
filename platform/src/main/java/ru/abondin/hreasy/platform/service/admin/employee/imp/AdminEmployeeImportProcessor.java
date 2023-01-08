package ru.abondin.hreasy.platform.service.admin.employee.imp;

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
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsRepo;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeeExcelRowDto;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportProcessStats;
import ru.abondin.hreasy.platform.service.dict.DictService;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import javax.annotation.PostConstruct;
import java.io.IOException;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.List;
import java.util.Locale;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Parse excel, validate and format all fields, find dicts by text value, prepare diff with database patch
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminEmployeeImportProcessor {
    private final I18Helper i18n;
    private final HrEasyCommonProps props;

    private DateTimeFormatter formatter;

    private final DictService dictService;
    private final AdminEmployeeExcelImporter exporter;
    private final EmployeeWithAllDetailsRepo emplRepo;


    @PostConstruct
    public void postConstruct() {
        this.formatter = DateTimeFormatter.ofPattern(props.getImportEmployee().getDateFormat());
    }


    public Mono<ImportProcessingResult> applyConfigAndParseExcelFile(AuthContext auth,
                                                                     EmployeeImportConfig config,
                                                                     Resource excel,
                                                                     Locale locale) {
        return Mono.defer(() -> {
                    try {
                        return Mono.just(excel.getInputStream());
                    } catch (IOException e) {
                        log.error("Unable to parse excel file", e);
                        return Mono.error(new BusinessError("errors.import.unexpectedError"));
                    }
                }).flatMap(excelStream ->
                        // 1. Parse the Excel
                        exporter.importEmployees(config, excelStream)
                                .collectList()
                                .flatMap(fromExcel -> {
                                    // 2. Load required dictionaries
                                    return Mono.zip(dictService.findPositions(auth)
                                                    .collectList(), dictService.findDepartments(auth).collectList())
                                            .flatMap(dicts -> {
                                                // 3. Load employees
                                                return emplRepo.findByEmailsInLowerCase(
                                                        fromExcel.stream().map(
                                                                        e -> e.getEmail().trim().toLowerCase(locale))
                                                                .collect(Collectors.toSet())
                                                ).collectList().map(employees -> {
                                                            // 4. Merge excel with existing employees and find the diff
                                                            merge(fromExcel, new ImportContext(employees, dicts.getT1(), dicts.getT2(), locale));
                                                            return fromExcel;
                                                        }
                                                );
                                            });
                                }))
                .map(ImportProcessingResult::new);
    }


    private void merge(List<ImportEmployeeExcelRowDto> fromExcel,
                       ImportContext context) {
        for (var excelRow : fromExcel) {
            parseRawData(excelRow, context);
        }
    }

    private void parseRawData(ImportEmployeeExcelRowDto excelRow, ImportContext context) {
        var trimmedEmailInLowerCase = excelRow.getEmail().toLowerCase(context.locale).trim();
        var existingEmpl = context.employees.stream().filter(e -> e.getEmail().trim().toLowerCase(context.locale)
                .equals(trimmedEmailInLowerCase)).findFirst();

        // Do not update email for existing employee!
        excelRow.setEmail(existingEmpl.map(EmployeeEntry::getEmail).orElse(trimmedEmailInLowerCase));

        excelRow.setEmployeeId(existingEmpl.map(EmployeeWithAllDetailsEntry::getId).orElse(null));

        Function<EmployeeWithAllDetailsEntry, SimpleDictDto> currentEmployeeDepartmentMapper
                = e -> e.getDepartmentId() == null ? null :
                context.departments.stream().filter(d -> d.getId() == e.getDepartmentId()).findFirst().orElse(null);

        Function<EmployeeWithAllDetailsEntry, SimpleDictDto> currentEmployeePositionMapper
                = e -> e.getPositionId() == null ? null :
                context.positions.stream().filter(d -> d.getId() == e.getPositionId()).findFirst().orElse(null);


        apply(excelRow.getBirthday(), existingEmpl, EmployeeWithAllDetailsEntry::getBirthday, context, this::applyLocalDate);
        apply(excelRow.getDepartment(), existingEmpl, currentEmployeeDepartmentMapper, context, (p, c) -> applyDict(p, c, c.departments));
        apply(excelRow.getPosition(), existingEmpl, currentEmployeePositionMapper, context, (p, c) -> applyDict(p, c, c.positions));
        apply(excelRow.getDisplayName(), existingEmpl, EmployeeWithAllDetailsEntry::getDisplayName, context, this::applyStringWithTrim);
        apply(excelRow.getDateOfDismissal(), existingEmpl, EmployeeWithAllDetailsEntry::getDateOfDismissal, context, this::applyLocalDate);
        apply(excelRow.getDateOfEmployment(), existingEmpl, EmployeeWithAllDetailsEntry::getDateOfEmployment, context, this::applyLocalDate);
        apply(excelRow.getDocumentIssuedDate(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentIssuedDate, context, this::applyLocalDate);
        apply(excelRow.getDocumentIssuedBy(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentIssuedBy, context, this::applyStringWithTrim);
        apply(excelRow.getDocumentSeries(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentSeries, context, this::applyStringWithTrim);
        apply(excelRow.getDocumentNumber(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentNumber, context, this::applyStringWithTrim);
        apply(excelRow.getExternalErpId(), existingEmpl, EmployeeWithAllDetailsEntry::getExtErpId, context, this::applyStringWithTrim);
        apply(excelRow.getRegistrationAddress(), existingEmpl, EmployeeWithAllDetailsEntry::getRegistrationAddress, context, this::applyStringWithTrim);
        apply(excelRow.getPhone(), existingEmpl, EmployeeWithAllDetailsEntry::getPhone, context, this::applyStringWithTrim);
        apply(excelRow.getSex(), existingEmpl, EmployeeWithAllDetailsEntry::getSex, context, this::applySex);
    }


    private <T> void apply(ImportEmployeeExcelRowDto.DataProperty<T> prop,
                           Optional<EmployeeWithAllDetailsEntry> existingEmployee,
                           Function<EmployeeWithAllDetailsEntry, T> existingEmployeeProp,
                           ImportContext context,
                           BiConsumer<ImportEmployeeExcelRowDto.DataProperty<T>, ImportContext> mapper) {
        prop.setCurrentValue(existingEmployee.map(existingEmployeeProp).orElse(null));
        if (prop.getRaw() != null) {
            mapper.accept(prop, context);
        }
    }

    private void applyDict(ImportEmployeeExcelRowDto.DataProperty<SimpleDictDto> prop,
                           ImportContext context,
                           List<SimpleDictDto> dict) {
        var value = prop.getRaw().trim().toLowerCase(context.locale);
        var key = dict
                .stream()
                .filter(d -> d.getName().toLowerCase(context.locale).trim().equals(value))
                .findFirst();
        if (key.isPresent()) {
            prop.setImportedValue(key.get());
        } else {
            prop.setError(i18n.localize("errors.import.not_in_dict"));
        }
    }

    private void applyLocalDate(ImportEmployeeExcelRowDto.DataProperty<LocalDate> prop, ImportContext ctx) {
        try {
            var date = formatter.parse(prop.getRaw().trim(), LocalDate::from);
            prop.setImportedValue(date);
        } catch (DateTimeParseException ex) {
            prop.setError(i18n.localize("errors.import.invalid_date_format", props.getImportEmployee().getDateFormat()));
        }
    }

    private void applyStringWithTrim(ImportEmployeeExcelRowDto.DataProperty<String> prop, ImportContext ctx) {
        prop.setImportedValue(prop.getRaw().trim());
    }

    private void applySex(ImportEmployeeExcelRowDto.DataProperty<String> prop, ImportContext ctx) {
        var value = prop.getRaw().toLowerCase(ctx.locale).replaceAll("\\s", "");
        if (props.getImportEmployee().getSexMaleVariants().contains(value)) {
            prop.setImportedValue(props.getImportEmployee().getSexDefaultMaleValue());
        } else if (props.getImportEmployee().getSexFemaleVariants().contains(value)) {
            prop.setImportedValue(props.getImportEmployee().getSexDefaultFemaleValue());
        } else {
            prop.setError(i18n.localize("errors.import.not_in_dict"));
        }
    }

    private record ImportContext(List<EmployeeWithAllDetailsEntry> employees, List<SimpleDictDto> positions,
                                 List<SimpleDictDto> departments, Locale locale) {
    }

    @Data
    public static class ImportProcessingResult {
        private final List<ImportEmployeeExcelRowDto> rows;
        private final ImportProcessStats stats;

        public ImportProcessingResult(List<ImportEmployeeExcelRowDto> rows) {
            this.rows = rows;
            int processedRows = rows.size();
            int errors = 0;
            int newItems = 0;
            int updatedItems = 0;
            for (var row : rows) {
                if (row.getErrorCount() > 0) {
                    errors += row.getErrorCount();
                }
                if (row.isNew()) {
                    newItems++;
                } else if (row.getUpdatedCellsCount() > 0) {
                    updatedItems++;
                }
            }
            this.stats = new ImportProcessStats(processedRows, errors, newItems, updatedItems);
        }
    }

}
