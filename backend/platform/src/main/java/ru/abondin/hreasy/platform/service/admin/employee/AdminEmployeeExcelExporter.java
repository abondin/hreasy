package ru.abondin.hreasy.platform.service.admin.employee;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.excel.ExcelReportFactory;
import ru.abondin.hreasy.platform.service.excel.ExcelReportFactory.Column;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeExportDto;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeExportFilter;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeKidExportDto;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

/** Writes selected employee fields and optional children as Excel tables. */
@Component
@RequiredArgsConstructor
public class AdminEmployeeExcelExporter {

    private final I18Helper i18Helper;
    private final ExcelReportFactory reportFactory;

    @Data
    @Builder
    public static class AdminEmployeeExportBundle {
        private OffsetDateTime exportTime;
        private List<EmployeeExportDto> employees;
        private List<EmployeeKidExportDto> kids;
        private Locale locale;
        private EmployeeExportFilter filter;
    }

    /** Only selected columns are written to the workbook, including its shared strings. */
    public void exportEmployees(AdminEmployeeExportBundle bundle, OutputStream out) throws IOException {
        try (var report = reportFactory.create(bundle.getLocale())) {
            var exportedAt = i18Helper.formatDateTime(bundle.getLocale(), bundle.getExportTime());
            var employees = bundle.getEmployees().stream()
                    .sorted(Comparator.comparing(EmployeeExportDto::getDisplayName)).toList();
            report.addSheet("export.employees.sheet.employees", "Employees", "export.employees.title",
                    "export.employees.exportedAt", exportedAt, employeeColumns(bundle.getFilter()), employees);
            if (bundle.getFilter().isIncludeKids()) {
                List<Column<EmployeeKidExportDto>> columns = List.of(
                        new Column<>("export.employees.column.parentName", 35, kid -> kid.getParent().getDisplayName()),
                        new Column<>("export.employees.column.parentEmail", 30, kid -> kid.getParent().getEmail()),
                        new Column<>("export.employees.column.kidName", 35, EmployeeKidExportDto::getDisplayName),
                        new Column<>("export.employees.column.kidBirthday", 18, EmployeeKidExportDto::getBirthday),
                        new Column<>("export.employees.column.age", 12, EmployeeKidExportDto::getAge));
                var kids = bundle.getKids().stream()
                        .sorted(Comparator.comparing(kid -> kid.getParent().getDisplayName())).toList();
                report.addSheet("export.employees.sheet.children", "Children", "export.employees.title",
                        "export.employees.exportedAt", exportedAt, columns, kids);
            }
            report.write(out);
        }
    }

    private List<Column<EmployeeExportDto>> employeeColumns(EmployeeExportFilter options) {
        var columns = new ArrayList<Column<EmployeeExportDto>>();
        columns.add(new Column<>("export.employees.column.displayName", 35, EmployeeExportDto::getDisplayName));
        columns.add(new Column<>("export.employees.column.email", 30, EmployeeExportDto::getEmail));
        columns.add(new Column<>("export.employees.column.department", 30, EmployeeExportDto::getDepartment));
        columns.add(new Column<>("export.employees.column.ba", 25, EmployeeExportDto::getBa));
        columns.add(new Column<>("export.employees.column.currentProject", 30, EmployeeExportDto::getCurrentProject));
        columns.add(new Column<>("export.employees.column.currentProjectRole", 25, EmployeeExportDto::getCurrentProjectRole));
        columns.add(new Column<>("export.employees.column.position", 30, EmployeeExportDto::getPosition));
        columns.add(new Column<>("export.employees.column.office", 30, EmployeeExportDto::getOffice));
        columns.add(new Column<>("export.employees.column.officeWorkplace", 25, EmployeeExportDto::getOfficeWorkplace));
        if (options.isIncludeAdditional()) {
            columns.add(new Column<>("export.employees.column.phone", 20, EmployeeExportDto::getPhone));
            columns.add(new Column<>("export.employees.column.telegram", 20, EmployeeExportDto::getTelegram));
            columns.add(new Column<>("export.employees.column.sex", 12, EmployeeExportDto::getSex));
            columns.add(new Column<>("export.employees.column.birthday", 18, EmployeeExportDto::getBirthday));
            columns.add(new Column<>("export.employees.column.organization", 30, EmployeeExportDto::getOrganization));
            columns.add(new Column<>("export.employees.column.dateOfEmployment", 22, EmployeeExportDto::getDateOfEmployment));
            columns.add(new Column<>("export.employees.column.level", 22, EmployeeExportDto::getLevel));
            columns.add(new Column<>("export.employees.column.officeLocation", 30, EmployeeExportDto::getOfficeLocation));
            columns.add(new Column<>("export.employees.column.spouseName", 35, EmployeeExportDto::getSpouseName));
            columns.add(new Column<>("export.employees.column.workDay", 20, EmployeeExportDto::getWorkDay));
            columns.add(new Column<>("export.employees.column.workType", 25, EmployeeExportDto::getWorkType));
            columns.add(new Column<>("export.employees.column.cityOfResidence", 25, EmployeeExportDto::getCityOfResidence));
            columns.add(new Column<>("export.employees.column.familyStatus", 22, EmployeeExportDto::getFamilyStatus));
            columns.add(new Column<>("export.employees.column.englishLevel", 22, EmployeeExportDto::getEnglishLevel));
            columns.add(new Column<>("export.employees.column.dateOfDismissal", 18, EmployeeExportDto::getDateOfDismissal));
        }
        if (options.isIncludePassport()) {
            columns.add(new Column<>("export.employees.column.documentFull", 50, EmployeeExportDto::getDocumentFull));
            columns.add(new Column<>("export.employees.column.registrationAddress", 50, EmployeeExportDto::getRegistrationAddress));
            columns.add(new Column<>("export.employees.column.foreignPassport", 35, EmployeeExportDto::getForeignPassport));
        }
        if (options.isIncludeKids()) {
            columns.add(new Column<>("export.employees.column.children", 35, EmployeeExportDto::getChildren));
        }
        return columns;
    }
}
