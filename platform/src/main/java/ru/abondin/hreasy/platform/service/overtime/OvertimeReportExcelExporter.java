package ru.abondin.hreasy.platform.service.overtime;

import lombok.Builder;
import lombok.Data;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.usermodel.HorizontalAlignment;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.springframework.context.MessageSource;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeEmployeeSummary;

import java.io.IOException;
import java.io.OutputStream;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Export overtime report and save it to disk
 */
@Component
@Slf4j
@RequiredArgsConstructor
//TODO Use messageSource
public class OvertimeReportExcelExporter {

    private final I18Helper i18Helper;

    @Data
    @Builder
    public static class OvertimeExportBundle {
        private final int period;
        private List<OvertimeEmployeeSummary> overtimes;
        private List<EmployeeDto> employees;
        private List<SimpleDictDto> projects;
        private OffsetDateTime exportTime;
    }


    public void exportReportForPeriod(OvertimeExportBundle bundle, OutputStream out) throws IOException {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            var styles = styles(wb);
            var mainSheet = createMainSheet(wb, styles, bundle);
            wb.write(out);
        }
    }


    private XSSFSheet createMainSheet(XSSFWorkbook wb, Map<String, XSSFCellStyle> styles, OvertimeExportBundle bundle) {
        var sheet = wb.createSheet(i18Helper.localize("overtimeexport.mainsheet"));
        var firstRowIndex = createTitle(wb, sheet, styles, bundle);
        var rowIndex = firstRowIndex;
        XSSFRow row;
        XSSFCell cell;
        row = sheet.createRow(rowIndex++);
        cell = row.createCell(0);
        cell.setCellValue(i18Helper.localize("overtimeexport.table.employee"));
        cell.setCellStyle(styles.get("generalStyle"));
        cell = row.createCell(1);
        cell.setCellValue(i18Helper.localize("overtimeexport.table.currentproject"));
        cell.setCellStyle(styles.get("generalStyle"));
        cell = row.createCell(2);
        cell.setCellValue(i18Helper.localize("overtimeexport.table.hours"));
        cell.setCellStyle(styles.get("generalStyle"));
        cell = row.createCell(3);
        cell.setCellValue(i18Helper.localize("overtimeexport.table.approve"));
        cell.setCellStyle(styles.get("generalStyle"));

        for (var e : bundle.employees.stream().sorted(Comparator.comparing(EmployeeDto::getDisplayName)).collect(Collectors.toList())) {
            var report = bundle.overtimes.stream()
                    .filter(r -> r.getEmployeeId() == e.getId()).findFirst().orElse(null);
            if (report == null) {
                // no overtimes for this employee
                continue;
            }
            row = sheet.createRow(rowIndex++);
            var currentProject = i18Helper.localize("overtimeexport.noproject");
            if (e.getCurrentProject() != null) {
                currentProject = bundle.projects.stream().filter(p -> p.getId() == e.getCurrentProject().getId())
                        .findFirst()
                        .orElseThrow(() -> new RuntimeException("Overtime export error: Unable to find current project for employee with id=" + e.getId()))
                        .getName();
            }
            cell = row.createCell(0);
            cell.setCellValue(e.getDisplayName());
            cell.setCellStyle(styles.get("generalStyle"));
            cell = row.createCell(1);
            cell.setCellValue(currentProject);
            cell.setCellStyle(styles.get("generalStyle"));
            cell = row.createCell(2);
            cell.setCellValue(report.getTotalHours());
            cell.setCellStyle(styles.get("generalStyle"));
            cell = row.createCell(3);
            cell.setCellValue(i18Helper.localize("enum.OvertimeApprovalCommonStatus." + report.getCommonApprovalStatus().toString()));
            cell.setCellStyle(styles.get("generalStyle"));
        }
        createTable(wb, sheet, firstRowIndex, rowIndex--);
        sheet.setColumnWidth(0, 256 * 60);
        sheet.setColumnWidth(1, 256 * 60);
        sheet.setColumnWidth(2, 256 * 10);
        sheet.setColumnWidth(3, 256 * 50);
        return sheet;
    }

    private int createTitle(XSSFWorkbook wb, XSSFSheet sheet, Map<String, XSSFCellStyle> styles, OvertimeExportBundle bundle) {
        var row = sheet.createRow((short) 0);
        var cell = row.createCell((short) 0);
        cell.setCellValue(new XSSFRichTextString(i18Helper.localize("overtimeexport.title")));
        cell.setCellStyle(styles.get("titleStyle"));

        row = sheet.createRow((short) 1);
        cell = row.createCell((short) 0);
        cell.setCellValue(new XSSFRichTextString(i18Helper.localize("overtimeexport.period") + ":"));
        cell.setCellStyle(styles.get("generalStyle"));
        cell = row.createCell((short) 1);
        cell.setCellValue(new XSSFRichTextString(formatPeriod(bundle.period)));
        cell.setCellStyle(styles.get("generalRightStyle"));

        row = sheet.createRow((short) 2);
        cell = row.createCell((short) 0);
        cell.setCellValue(new XSSFRichTextString(i18Helper.localize("overtimeexport.exported.at") + ":"));
        cell.setCellStyle(styles.get("generalStyle"));
        cell = row.createCell((short) 1);
        cell.setCellValue(bundle.getExportTime().toLocalDateTime());
        cell.setCellStyle(styles.get("dateTimeStyle"));


        sheet.addMergedRegion(new CellRangeAddress(0, 0, 0, 3));
        sheet.addMergedRegion(new CellRangeAddress(1, 1, 1, 3));
        sheet.addMergedRegion(new CellRangeAddress(2, 2, 1, 3));
        return 5;
    }


    private Map<String, XSSFCellStyle> styles(XSSFWorkbook wb) {
        var titleFont = wb.createFont();
        titleFont.setFontHeightInPoints((short) 14);
        titleFont.setFontName("Times New Roman");
        var titleStyle = wb.createCellStyle();
        titleStyle.setFont(titleFont);

        var generalFont = wb.createFont();
        generalFont.setFontHeightInPoints((short) 12);
        generalFont.setFontName("Times New Roman");
        var generalStyle = wb.createCellStyle();
        generalStyle.setFont(generalFont);

        var generalRightStyle = wb.createCellStyle();
        generalRightStyle.setFont(generalFont);
        generalRightStyle.setAlignment(HorizontalAlignment.RIGHT);

        var dateTimeStyle = wb.createCellStyle();
        dateTimeStyle.setDataFormat(wb.getCreationHelper().createDataFormat().getFormat("dd/mm/yyyy h:mm"));
        dateTimeStyle.setFont(generalFont);

        return Map.of("titleStyle", titleStyle,
                "generalStyle", generalStyle,
                "dateTimeStyle", dateTimeStyle,
                "generalRightStyle", generalRightStyle);
    }

    public static String formatPeriod(int periodId) {
        var ym = YearMonth.of(periodId / 100, periodId % 100 + 1);
        return ym.format(DateTimeFormatter.ofPattern("MM/yyyy", new Locale("ru")));
    }

    private void createTable(XSSFWorkbook wb, XSSFSheet sheet, int firstRowIndex, int lastRowIndex) {
        // Set which area the table should be placed in
        var reference = wb.getCreationHelper().createAreaReference(
                new CellReference(firstRowIndex, 0), new CellReference(lastRowIndex, 3));
        var table = sheet.createTable(reference);
        table.setName("overtime_table");

        table.getCTTable().addNewTableStyleInfo();
        table.getCTTable().getTableStyleInfo().setName("TableStyleMedium3");

        // Style the table
        var style = (XSSFTableStyleInfo) table.getStyle();
        style.setName("TableStyleMedium3");
        style.setShowRowStripes(true);
    }
}
