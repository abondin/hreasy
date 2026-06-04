package ru.abondin.hreasy.platform.service.admin.employee.kids.imp;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsEntry;
import ru.abondin.hreasy.platform.repo.employee.admin.EmployeeWithAllDetailsRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.kids.EmployeeKidRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.kids.EmployeeKidView;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto.EmployeeKidImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.kids.imp.dto.ImportEmployeeKidExcelRowDto;
import ru.abondin.hreasy.platform.service.admin.imp.ExcelImportProcessorUtils;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportContext;
import ru.abondin.hreasy.platform.service.admin.imp.dto.ExcelImportProcessingResult;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.io.IOException;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

/**
 * Parse excel, validate and format all fields, find dicts by text value, prepare diff with database patch
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminEmployeeKidImportProcessor {


    private final AdminEmployeeKidsExcelImporter importer;
    private final EmployeeKidRepo kidsRepo;
    private final EmployeeWithAllDetailsRepo emplRepo;
    private final DateTimeService dateTimeService;

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
                                        // 2. Load employees
                                        emplRepo.findByEmailsInLowerCase(
                                                fromExcel.stream().map(
                                                                e -> e.getParentEmail().trim().toLowerCase(locale))
                                                        .collect(Collectors.toSet())
                                        ).collectList().flatMap(employees ->
                                                // 3. Load existing kids
                                                kidsRepo.findAllKidsWithParentInfo(dateTimeService.now())
                                                        .collectList().map(kids -> {
                                                                    // 4. Merge excel with existing employees and find the diff
                                                                    merge(fromExcel, new ExcelImportContext<>(kids, locale), employees);
                                                                    return fromExcel;
                                                                }
                                                        )

                                        )))
                .map(ExcelImportProcessingResult::new);
    }


    private void merge(List<ImportEmployeeKidExcelRowDto> fromExcel,
                       ExcelImportContext<EmployeeKidView> context,
                       List<EmployeeWithAllDetailsEntry> employees) {
        for (var excelRow : fromExcel) {
            parseRawData(excelRow, context, employees);
        }
    }

    private void parseRawData(ImportEmployeeKidExcelRowDto excelRow,
                              ExcelImportContext<EmployeeKidView> context,
                              List<EmployeeWithAllDetailsEntry> employees
    ) {
        var trimmedParentEmailInLowerCase = excelRow.getParentEmail().toLowerCase(context.locale()).trim();

        var existingParentEmployee = employees.stream().filter(e ->
                e.getEmail().trim().toLowerCase(context.locale())
                        .equals(trimmedParentEmailInLowerCase)).findFirst().orElseThrow(
                                () -> new BusinessError("errors.import.kids.parent.notFound", trimmedParentEmailInLowerCase,
                                        Integer.toString(excelRow.getRowNumber())));

        excelRow.setParent(new SimpleDictDto(existingParentEmployee.getId(), existingParentEmployee.getDisplayName()));

        var trimmedDisplayName = excelRow.getDisplayName().toLowerCase(context.locale()).trim();
        var existingKid = context.entries().stream().filter(e ->
                e.getParent().equals(existingParentEmployee.getId())
                        &&
                        e.getDisplayName().trim().toLowerCase(context.locale())
                                .equals(trimmedDisplayName)
        ).findFirst();

        excelRow.setEmployeeKidId(existingKid.map(EmployeeKidView::getId).orElse(null));
        processorUtils.apply(excelRow.getBirthday(), existingKid, EmployeeKidView::getBirthday, processorUtils::applyLocalDate);
    }


}
