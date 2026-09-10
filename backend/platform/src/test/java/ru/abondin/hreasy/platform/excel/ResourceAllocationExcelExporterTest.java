package ru.abondin.hreasy.platform.excel;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.WorkbookFactory;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import ru.abondin.hreasy.platform.service.allocation.ResourceAllocationExcelExporter;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto.AllocationDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto.EmployeeDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto.ProjectDto;
import ru.abondin.hreasy.platform.service.dto.ProjectWorkstreamDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class ResourceAllocationExcelExporterTest {
    private final ResourceAllocationExcelExporter exporter = new ResourceAllocationExcelExporter();

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void rendersNumericRowsSeparatingWorkstreamsAndPreservingMissingAndZero(boolean percentages) throws Exception {
        var analytics = new ResourceAllocationAnalyticsDto(2026,
                List.of(new EmployeeDto(301, "Alex Morgan", null, null, null, null,
                        "Developer", "alex.morgan@example.test")),
                List.of(new ProjectDto(401, "=2+2", null, null, 601, "Example account", null, null, true, false)),
                List.of(new ProjectWorkstreamDto(501, null, "Delivery", null)),
                List.of(new AllocationDto(202600, 301, 401, null, 50),
                        new AllocationDto(202601, 301, 401, null, 0),
                        new AllocationDto(202611, 301, 401, null, 25),
                        new AllocationDto(202600, 301, 401, 501, 100)));
        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(render(analytics, percentages)))) {
            var sheet = workbook.getSheetAt(0);
            assertEquals(6, sheet.getLastRowNum());
            assertEquals(19, sheet.getRow(4).getLastCellNum());
            assertEquals("За год", sheet.getRow(4).getCell(6).getStringCellValue());
            var projectRow = sheet.getRow(5);
            assertEquals("Alex Morgan", projectRow.getCell(0).getStringCellValue());
            assertEquals("alex.morgan@example.test", projectRow.getCell(1).getStringCellValue());
            assertEquals("Example account", projectRow.getCell(2).getStringCellValue());
            assertEquals(CellType.STRING, projectRow.getCell(3).getCellType());
            assertEquals("=2+2", projectRow.getCell(3).getStringCellValue());
            assertEquals("Developer", projectRow.getCell(5).getStringCellValue());
            assertEquals(CellType.NUMERIC, projectRow.getCell(6).getCellType());
            assertEquals(0.75, projectRow.getCell(6).getNumericCellValue(), 0.000001);
            assertEquals(0.5, projectRow.getCell(7).getNumericCellValue(), 0.000001);
            assertEquals(CellType.NUMERIC, projectRow.getCell(8).getCellType());
            assertEquals(0, projectRow.getCell(8).getNumericCellValue());
            assertTrue(projectRow.getCell(9) == null || projectRow.getCell(9).getCellType() == CellType.BLANK);
            assertEquals(0.25, projectRow.getCell(18).getNumericCellValue(), 0.000001);
            assertEquals(percentages, projectRow.getCell(7).getCellStyle().getDataFormatString().contains("%"));
            assertEquals("Delivery", sheet.getRow(6).getCell(4).getStringCellValue());
            assertEquals(1, sheet.getRow(6).getCell(6).getNumericCellValue());
            assertNotNull(sheet.getPaneInformation());
            assertEquals(sheet.getLastRowNum(), ((XSSFSheet) sheet).getTables().getFirst().getEndRowIndex());
            assertTrue(sheet.getRow(2).getCell(1).getStringCellValue().contains("JUnit test"));
            for (var row : sheet) {
                for (var cell : row) {
                    if (cell.getCellType() == CellType.STRING) {
                        assertFalse(cell.getStringCellValue().contains("${"), "Unresolved JXLS expression");
                    }
                }
            }
        }
    }

    @Test
    void exportsAnEmptyYearWithHeadersAndNoFakeDataRow() throws Exception {
        var empty = new ResourceAllocationAnalyticsDto(2026, List.of(), List.of(), List.of(), List.of());
        try (var workbook = WorkbookFactory.create(new ByteArrayInputStream(render(empty, false)))) {
            var sheet = workbook.getSheetAt(0);
            assertEquals("Сотрудник", sheet.getRow(4).getCell(0).getStringCellValue());
            assertEquals(sheet.getLastRowNum(), ((XSSFSheet) sheet).getTables().getFirst().getEndRowIndex());
            for (int index = 5; index <= sheet.getLastRowNum(); index++) {
                var row = sheet.getRow(index);
                if (row == null) continue;
                for (var cell : row) assertEquals(CellType.BLANK, cell.getCellType());
            }
        }
    }

    private byte[] render(ResourceAllocationAnalyticsDto analytics, boolean percentages) throws Exception {
        exporter.setTemplate(new ClassPathResource("jxls/resource_allocations_template.xlsx"));
        try (var output = new ByteArrayOutputStream()) {
            exporter.export(analytics, percentages, OffsetDateTime.parse("2026-09-09T12:00:00Z"), "JUnit test", output);
            return output.toByteArray();
        }
    }
}
