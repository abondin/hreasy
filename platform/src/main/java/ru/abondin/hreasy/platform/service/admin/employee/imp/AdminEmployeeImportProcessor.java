package ru.abondin.hreasy.platform.service.admin.employee.imp;

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
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeeExcelDto;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeesWorkflowDto;
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


    public Mono<List<ImportEmployeeExcelDto>> applyConfigAndParseExcelFile(AuthContext auth,
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
                        }));
    }


    private void merge(List<ImportEmployeeExcelDto> fromExcel,
                       ImportContext context) {
        for (var excelRow : fromExcel) {
            parseRawData(excelRow, context);
        }
    }

    private void parseRawData(ImportEmployeeExcelDto excelRow, ImportContext context) {
        var existingEmpl = context.employees.stream().filter(e -> e.getEmail().trim().toLowerCase(context.locale)
                .equals(excelRow.getEmail().toLowerCase(context.locale).trim())).findFirst();

        excelRow.setEmployeeId(existingEmpl.map(EmployeeWithAllDetailsEntry::getId).orElse(null));

        apply(excelRow.getBirthday(), existingEmpl, EmployeeWithAllDetailsEntry::getBirthday, context, this::applyLocalDate);
        apply(excelRow.getDepartment(), existingEmpl, EmployeeWithAllDetailsEntry::getDepartmentId, context, (p, c) -> applyDict(p, c, c.departments));
        apply(excelRow.getPosition(), existingEmpl, EmployeeWithAllDetailsEntry::getPositionId, context, (p, c) -> applyDict(p, c, c.positions));
        apply(excelRow.getDisplayName(), existingEmpl, EmployeeWithAllDetailsEntry::getDisplayName, context, this::applyStringWithTrim);
        apply(excelRow.getDateOfDismissal(), existingEmpl, EmployeeWithAllDetailsEntry::getDateOfDismissal, context, this::applyLocalDate);
        apply(excelRow.getDateOfEmployment(), existingEmpl, EmployeeWithAllDetailsEntry::getDateOfEmployment, context, this::applyLocalDate);
        apply(excelRow.getDocumentIssuedDate(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentIssuedDate, context, this::applyLocalDate);
        apply(excelRow.getDocumentIssuedBy(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentIssuedBy, context, this::applyStringWithTrim);
        apply(excelRow.getDocumentSeries(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentSeries, context, this::applyStringWithTrim);
        apply(excelRow.getDocumentNumber(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentNumber, context, this::applyStringWithTrim);
        apply(excelRow.getExternalErpId(), existingEmpl, EmployeeWithAllDetailsEntry::getExtErpId, context, this::applyStringWithTrim);
        apply(excelRow.getRegistrationAddress(), existingEmpl, EmployeeWithAllDetailsEntry::getRegistrationAddress, context, this::applyStringWithTrim);
        // TODO Set phone datatype in database to string
        // apply(excelRow.getPhone(), existingEmpl, EmployeeWithAllDetailsEntry::getPhone, context, this::applyStringWithTrim);
        apply(excelRow.getSex(), existingEmpl, EmployeeWithAllDetailsEntry::getSex, context, this::applySex);
    }


    private <T> void apply(ImportEmployeeExcelDto.DataProperty<T> prop,
                           Optional<EmployeeWithAllDetailsEntry> existingEmployee,
                           Function<EmployeeWithAllDetailsEntry, T> existingEmployeeProp,
                           ImportContext context,
                           BiConsumer<ImportEmployeeExcelDto.DataProperty<T>, ImportContext> mapper) {
        prop.setCurrentValue(existingEmployee.map(existingEmployeeProp).orElse(null));
        if (prop.getRaw() != null) {
            mapper.accept(prop, context);
        }
    }

    private void applyDict(ImportEmployeeExcelDto.DataProperty<Integer> prop,
                           ImportContext context,
                           List<SimpleDictDto> dict) {
        var value = prop.getRaw().trim().toLowerCase(context.locale);
        var key = dict
                .stream()
                .filter(d -> d.getName().toLowerCase(context.locale).trim().equals(value))
                .findFirst();
        if (key.isPresent()) {
            prop.setImportedValue(key.get().getId());
        } else {
            prop.setError(i18n.localize("errors.import.not_in_dict"));
        }
    }

    private void applyLocalDate(ImportEmployeeExcelDto.DataProperty<LocalDate> prop, ImportContext ctx) {
        try {
            var date = formatter.parse(prop.getRaw().trim(), LocalDate::from);
            prop.setImportedValue(date);
        } catch (DateTimeParseException ex) {
            prop.setError(i18n.localize("errors.import.invalid_date_format", props.getImportEmployee().getDateFormat()));
        }
    }

    private void applyStringWithTrim(ImportEmployeeExcelDto.DataProperty<String> prop, ImportContext ctx) {
        prop.setImportedValue(prop.getRaw().trim());
    }

    private void applySex(ImportEmployeeExcelDto.DataProperty<String> prop, ImportContext ctx) {
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
}
