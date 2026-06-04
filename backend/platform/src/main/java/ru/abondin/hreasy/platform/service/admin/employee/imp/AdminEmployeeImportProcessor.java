package ru.abondin.hreasy.platform.service.admin.employee.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsRepo;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeeExcelRowDto;
import ru.abondin.hreasy.platform.service.admin.imp.ExcelImportProcessorUtils;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportContext;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportProcessingResult;
import ru.abondin.hreasy.platform.service.dict.DictService;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Parse excel, validate and format all fields, find dicts by text value, prepare diff with database patch
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminEmployeeImportProcessor {


    private final DictService dictService;
    private final AdminEmployeeExcelImporter importer;
    private final EmployeeWithAllDetailsRepo emplRepo;

    private final ExcelImportProcessorUtils processorUtils;


    public Mono<ExcelImportProcessingResult<ImportEmployeeExcelRowDto>> applyConfigAndParseExcelFile(AuthContext auth,
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
                        importer.importFromFile(config, excelStream)
                                .collectList()
                                .flatMap(fromExcel ->
                                        // 2. Load required dictionaries
                                        Mono.zip(dictService.findPositions(auth)
                                                                .collectList(), dictService.findDepartments(auth).collectList(),
                                                        dictService.findOrganizations(auth).collectList())
                                                .flatMap(dicts ->
                                                        // 3. Load employees
                                                        emplRepo.findByEmailsInLowerCase(
                                                                fromExcel.stream().map(
                                                                                e -> e.getEmail().trim().toLowerCase(locale))
                                                                        .collect(Collectors.toSet())
                                                        ).collectList().map(employees -> {
                                                                    // 4. Merge excel with existing employees and find the diff
                                                                    merge(fromExcel, new ExcelImportContext<>(employees, locale),
                                                                            dicts.getT1(),
                                                                            dicts.getT2(),
                                                                            dicts.getT3()
                                                                    );
                                                                    return fromExcel;
                                                                }
                                                        )
                                                )
                                ))
                .map(ExcelImportProcessingResult::new);
    }


    private void merge(List<ImportEmployeeExcelRowDto> fromExcel,
                       ExcelImportContext<EmployeeWithAllDetailsEntry> context,
                       List<SimpleDictDto> positions,
                       List<SimpleDictDto> departments,
                       List<SimpleDictDto> organizations) {
        for (var excelRow : fromExcel) {
            parseRawData(excelRow, context, departments, positions, organizations);
        }
    }

    private void parseRawData(ImportEmployeeExcelRowDto excelRow,
                              ExcelImportContext<EmployeeWithAllDetailsEntry> context,
                              List<SimpleDictDto> departments,
                              List<SimpleDictDto> positions,
                              List<SimpleDictDto> organizations
    ) {
        var trimmedEmailInLowerCase = excelRow.getEmail().toLowerCase(context.locale()).trim();
        var existingEmpl = context.entries().stream().filter(e -> e.getEmail().trim().toLowerCase(context.locale())
                .equals(trimmedEmailInLowerCase)).findFirst();

        // Do not update email for existing employee!
        excelRow.setEmail(existingEmpl.map(EmployeeEntry::getEmail).orElse(trimmedEmailInLowerCase));

        excelRow.setEmployeeId(existingEmpl.map(EmployeeWithAllDetailsEntry::getId).orElse(null));

        Function<EmployeeWithAllDetailsEntry, SimpleDictDto> currentEmployeeDepartmentMapper
                = e -> e.getDepartmentId() == null ? null :
                departments.stream().filter(d -> d.getId() == e.getDepartmentId()).findFirst().orElse(null);

        Function<EmployeeWithAllDetailsEntry, SimpleDictDto> currentEmployeePositionMapper
                = e -> e.getPositionId() == null ? null :
                positions.stream().filter(d -> d.getId() == e.getPositionId()).findFirst().orElse(null);

        Function<EmployeeWithAllDetailsEntry, SimpleDictDto> currentEmployeeOrganizationMapper
                = e -> e.getOrganizationId() == null ? null :
                organizations.stream().filter(d -> d.getId() == e.getOrganizationId()).findFirst().orElse(null);


        processorUtils.apply(excelRow.getBirthday(), existingEmpl, EmployeeWithAllDetailsEntry::getBirthday, processorUtils::applyLocalDate);
        processorUtils.apply(excelRow.getDepartment(), existingEmpl, currentEmployeeDepartmentMapper, p -> processorUtils.applyDict(p, context, departments));
        processorUtils.apply(excelRow.getOrganization(), existingEmpl, currentEmployeeOrganizationMapper, p -> processorUtils.applyDict(p, context, organizations));
        processorUtils.apply(excelRow.getPosition(), existingEmpl, currentEmployeePositionMapper, p -> processorUtils.applyDict(p, context, positions));
        processorUtils.apply(excelRow.getDisplayName(), existingEmpl, EmployeeWithAllDetailsEntry::getDisplayName, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getDateOfDismissal(), existingEmpl, EmployeeWithAllDetailsEntry::getDateOfDismissal, processorUtils::applyLocalDate);
        processorUtils.apply(excelRow.getDateOfEmployment(), existingEmpl, EmployeeWithAllDetailsEntry::getDateOfEmployment, processorUtils::applyLocalDate);
        processorUtils.apply(excelRow.getDocumentIssuedDate(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentIssuedDate, processorUtils::applyLocalDate);
        processorUtils.apply(excelRow.getDocumentIssuedBy(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentIssuedBy, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getDocumentSeries(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentSeries, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getDocumentNumber(), existingEmpl, EmployeeWithAllDetailsEntry::getDocumentNumber, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getExternalErpId(), existingEmpl, EmployeeWithAllDetailsEntry::getExtErpId, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getRegistrationAddress(), existingEmpl, EmployeeWithAllDetailsEntry::getRegistrationAddress, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getPhone(), existingEmpl, EmployeeWithAllDetailsEntry::getPhone, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getSex(), existingEmpl, EmployeeWithAllDetailsEntry::getSex, processorUtils::applyStringWithTrim);
    }


}
