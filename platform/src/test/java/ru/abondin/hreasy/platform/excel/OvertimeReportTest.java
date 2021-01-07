package ru.abondin.hreasy.platform.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.poi.ss.util.CellReference;
import org.apache.poi.xssf.usermodel.*;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeEmployeeSummary;

import java.io.File;
import java.io.FileOutputStream;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Slf4j
public class OvertimeReportTest {

    private final List<String> employees = Arrays.asList(
            "Иванов Иван Иванович",
            "Петров Петр Петрович",
            "Сидоров Сидр Сидорочив",
            "Маринина Марина Павловна"
    );

    @Test
    @Disabled("Nothing to tests yet. It is only to play with apache poi")
    public void testSummary() throws Exception {
        try (XSSFWorkbook wb = new XSSFWorkbook()) {
            var sheet = wb.createSheet();

            XSSFRow row;
            XSSFCell cell;
            row = sheet.createRow(0);
            cell = row.createCell(0);
            cell.setCellValue("Сотрудники");

            cell = row.createCell(1);
            cell.setCellValue("Часы");


            int rowIndex = 1;
            for (var sum : getOvertimes()) {
                row = sheet.createRow(rowIndex);
                cell = row.createCell(0);
                cell.setCellValue(employees.get(sum.getEmployeeId()));
                cell = row.createCell(1);
                cell.setCellValue(sum.getTotalHours());
                rowIndex++;
            }

            createTable(wb, sheet, rowIndex--);

            sheet.autoSizeColumn(0);

            var destination = File.createTempFile("simpletable", ".xlsx");
            log.debug("Writing test file to {}", destination);
            // Save
            try (var fileOut = new FileOutputStream(destination)) {
                wb.write(fileOut);
            }
        }
    }

    private List<OvertimeEmployeeSummary> getOvertimes() {
        return Stream.generate(() ->
                new OvertimeEmployeeSummary() {{
                    setEmployeeId((int) (Math.random() * employees.size()));
                    setTotalHours((float) (Math.random() * 24));
                }}).limit(100).collect(Collectors.toList());
    }

    private void createTable(XSSFWorkbook wb, XSSFSheet sheet, int lastRowIndex) {
        // Set which area the table should be placed in
        var reference = wb.getCreationHelper().createAreaReference(
                new CellReference(0, 0), new CellReference(lastRowIndex, 1));
        var table = sheet.createTable(reference);
        table.setName("overtime_table");

        table.getCTTable().addNewTableStyleInfo();
        table.getCTTable().getTableStyleInfo().setName("TableStyleMedium2");

        // Style the table
        var style = (XSSFTableStyleInfo) table.getStyle();
        style.setName("TableStyleMedium2");
        style.setShowColumnStripes(false);
    }
}
