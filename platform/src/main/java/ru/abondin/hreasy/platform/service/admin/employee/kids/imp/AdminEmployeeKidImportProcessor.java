package ru.abondin.hreasy.platform.service.admin.employee.kids.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.kids.EmployeeKidRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.kids.EmployeeKidView;
import ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto.EmployeeKidImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto.ImportEmployeeKidExcelRowDto;
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
public class AdminEmployeeKidImportProcessor {


    private final DictService dictService;
    private final AdminEmployeeKidsExcelImporter importer;
    private final EmployeeKidRepo kidsRepo;

    private final ExcelImportProcessorUtils processorUtils;


    public Mono<ExcelImportProcessingResult<ImportEmployeeKidExcelRowDto>> applyConfigAndParseExcelFile(AuthContext auth,
                                                                                                        EmployeeKidImportConfig config,
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
                                                        kidsRepo.findByParentEmailAndDisplayNameInLowerCase(
                                                                fromExcel.stream().map(
                                                                                e -> Pair.of(
                                                                                        e.getParentEmail().trim().toLowerCase(locale),
                                                                                        e.getDisplayName().trim().toLowerCase(locale)))
                                                                        .collect(Collectors.toSet())
                                                        ).collectList().map(kids -> {
                                                                    // 4. Merge excel with existing employees and find the diff
                                                                    merge(fromExcel, new ExcelImportContext<>(kids, locale),
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


    private void merge(List<ImportEmployeeKidExcelRowDto> fromExcel,
                       ExcelImportContext<EmployeeKidView> context) {
        for (var excelRow : fromExcel) {
            parseRawData(excelRow, context);
        }
    }

    private void parseRawData(ImportEmployeeKidExcelRowDto excelRow,
                              ExcelImportContext<EmployeeKidView> context
    ) {
        var trimmedEmailInLowerCase = excelRow.getParentEmail().toLowerCase(context.locale()).trim();
        var trimmedDisplayName = excelRow.getDisplayName().toLowerCase(context.locale()).trim();
        var existingKid = context.entries().stream().filter(e ->
                e.getParent().trim().toLowerCase(context.locale())
                .equals(trimmedEmailInLowerCase)
        ).findFirst();

        // Do not update email for existing employee!
        excelRow.setEmail(existingKid.map(EmployeeEntry::getEmail).orElse(trimmedEmailInLowerCase));

        excelRow.setEmployeeId(existingKid.map(EmployeeWithAllDetailsEntry::getId).orElse(null));

        Function<EmployeeWithAllDetailsEntry, SimpleDictDto> currentEmployeeDepartmentMapper
                = e -> e.getDepartmentId() == null ? null :
                departments.stream().filter(d -> d.getId() == e.getDepartmentId()).findFirst().orElse(null);

        Function<EmployeeWithAllDetailsEntry, SimpleDictDto> currentEmployeePositionMapper
                = e -> e.getPositionId() == null ? null :
                positions.stream().filter(d -> d.getId() == e.getPositionId()).findFirst().orElse(null);

        Function<EmployeeWithAllDetailsEntry, SimpleDictDto> currentEmployeeOrganizationMapper
                = e -> e.getOrganizationId() == null ? null :
                organizations.stream().filter(d -> d.getId() == e.getOrganizationId()).findFirst().orElse(null);


        processorUtils.apply(excelRow.getBirthday(), existingKid, EmployeeWithAllDetailsEntry::getBirthday, processorUtils::applyLocalDate);
        processorUtils.apply(excelRow.getDepartment(), existingKid, currentEmployeeDepartmentMapper, p -> processorUtils.applyDict(p, context, departments));
        processorUtils.apply(excelRow.getOrganization(), existingKid, currentEmployeeOrganizationMapper, p -> processorUtils.applyDict(p, context, organizations));
        processorUtils.apply(excelRow.getPosition(), existingKid, currentEmployeePositionMapper, p -> processorUtils.applyDict(p, context, positions));
        processorUtils.apply(excelRow.getDisplayName(), existingKid, EmployeeWithAllDetailsEntry::getDisplayName, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getDateOfDismissal(), existingKid, EmployeeWithAllDetailsEntry::getDateOfDismissal, processorUtils::applyLocalDate);
        processorUtils.apply(excelRow.getDateOfEmployment(), existingKid, EmployeeWithAllDetailsEntry::getDateOfEmployment, processorUtils::applyLocalDate);
        processorUtils.apply(excelRow.getDocumentIssuedDate(), existingKid, EmployeeWithAllDetailsEntry::getDocumentIssuedDate, processorUtils::applyLocalDate);
        processorUtils.apply(excelRow.getDocumentIssuedBy(), existingKid, EmployeeWithAllDetailsEntry::getDocumentIssuedBy, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getDocumentSeries(), existingKid, EmployeeWithAllDetailsEntry::getDocumentSeries, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getDocumentNumber(), existingKid, EmployeeWithAllDetailsEntry::getDocumentNumber, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getExternalErpId(), existingKid, EmployeeWithAllDetailsEntry::getExtErpId, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getRegistrationAddress(), existingKid, EmployeeWithAllDetailsEntry::getRegistrationAddress, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getPhone(), existingKid, EmployeeWithAllDetailsEntry::getPhone, processorUtils::applyStringWithTrim);
        processorUtils.apply(excelRow.getSex(), existingKid, EmployeeWithAllDetailsEntry::getSex, processorUtils::applyStringWithTrim);
    }


}
