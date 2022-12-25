package ru.abondin.hreasy.platform.service.admin.employee;

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
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportEmployeesWorkflowEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.imp.ImportEmployeesWorkflowRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeImportMapper;
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
    private final DictService dictService;
    private final AdminEmployeeExcelImporter exporter;
    private final ImportEmployeesWorkflowRepo workflowRepo;
    private final DateTimeService dateTimeService;

    private final EmployeeImportMapper importMapper;

    private final HrEasyCommonProps props;

    private final I18Helper i18n;

    private DateTimeFormatter formatter;


    @PostConstruct
    public void postConstruct() {
        this.formatter = DateTimeFormatter.ofPattern(props.getImportEmployee().getDateFormat());
    }

    /**
     * First step in import process.
     *
     * @param auth
     * @return not completed or canceled process initiated by this user or start new import process
     */
    public Mono<ImportEmployeesWorkflowDto> startNewOrGetCurrentImportProcess(AuthContext auth) {
        log.info("Start new import employee process by {}", auth.getUsername());
        return workflowRepo.get(auth.getEmployeeInfo().getEmployeeId())
                .switchIfEmpty(workflowRepo.save(defaultImportConfig(auth)))
                .map(importMapper::fromEntry);
    }

    private ImportEmployeesWorkflowEntry defaultImportConfig(AuthContext auth) {
        var entry = new ImportEmployeesWorkflowEntry();
        entry.setState(0);
        entry.setConfig(importMapper.config(new EmployeeImportConfig()));
        entry.setCreatedAt(dateTimeService.now());
        entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
        return entry;
    }


    public Mono<ImportEmployeesWorkflowDto> startImportProcess(AuthContext auth,
                                                               EmployeeImportConfig config,
                                                               Resource inputFile,
                                                               Locale locale) {
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
                                                                    e -> e.getEmail().trim().toLowerCase(locale))
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
        var existingEmpl = context.employees.stream().filter(e -> e.getEmail().trim().toLowerCase(context.locale)
                .equals(excelRow.getEmail().toLowerCase(context.locale).trim())).findFirst();

        apply(excelRow.getBirthday(), existingEmpl, EmployeeWithAllDetailsEntry::getBirthday, context, this::applyLocalDate);
        apply(excelRow.getDepartment(), existingEmpl, EmployeeWithAllDetailsEntry::getDepartmentId, context, (p, c) -> applyDict(p, c, c.departments));
        apply(excelRow.getPosition(), existingEmpl, EmployeeWithAllDetailsEntry::getPositionId, context, (p, c) -> applyDict(p, c, c.positions));
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
        var value = prop.getRaw().toLowerCase(ctx.locale).replaceAll("\\W", "");
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
