package ru.abondin.hreasy.platform.excel;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.core.io.ClassPathResource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.I18Helper;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeExcelExporter;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeExportService;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.*;
import ru.abondin.hreasy.platform.service.ba.BusinessAccountService;
import ru.abondin.hreasy.platform.service.dict.DictService;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class AdminEmployeesExportedTest {
    private final OffsetDateTime now = OffsetDateTime.parse("2026-10-06T12:00:00+03:00");

    @Test
    void fillsBothSheetsWithoutSkypeAndExpandsTables() throws Exception {
        var parent = employee("Alex Morgan", "alex.morgan@example.test");
        var other = employee("Taylor Reed", "taylor.reed@example.test");
        var kids = List.of(new EmployeeKidExportDto(parent, "Casey Morgan", LocalDate.of(2018, 10, 6), 8),
                new EmployeeKidExportDto(parent, "Jordan Morgan", null, null),
                new EmployeeKidExportDto(other, "Sam Reed", LocalDate.of(2020, 11, 1), 5));
        try (var workbook = export(List.of(other, parent), kids)) {
            assertEquals(2, workbook.getNumberOfSheets());
            var employees = workbook.getSheet("Сотрудники");
            var children = workbook.getSheet("Дети");
            assertEquals(26, employees.getRow(2).getLastCellNum());
            for (var cell : employees.getRow(2)) {
                assertNotEquals("Skype", cell.getStringCellValue());
            }
            assertEquals("Alex Morgan", employees.getRow(3).getCell(0).getStringCellValue());
            assertEquals("Alex Morgan", children.getRow(3).getCell(0).getStringCellValue());
            assertEquals(parent.getEmail(), children.getRow(3).getCell(1).getStringCellValue());
            assertEquals("Casey Morgan", children.getRow(3).getCell(2).getStringCellValue());
            assertEquals(CellType.NUMERIC, children.getRow(3).getCell(3).getCellType());
            assertEquals(LocalDate.of(2018, 10, 6), children.getRow(3).getCell(3).getLocalDateTimeCellValue().toLocalDate());
            assertEquals(8, children.getRow(3).getCell(4).getNumericCellValue());
            assertEquals(CellType.BLANK, children.getRow(4).getCell(3).getCellType());
            assertEquals(CellType.BLANK, children.getRow(4).getCell(4).getCellType());
            assertEquals("A3:Z5", employees.getTables().getFirst().getArea().formatAsString());
            assertEquals("A3:Z5", employees.getTables().getFirst().getCTTable().getAutoFilter().getRef());
            assertEquals("A3:E6", children.getTables().getFirst().getArea().formatAsString());
            assertEquals("A3:E6", children.getTables().getFirst().getCTTable().getAutoFilter().getRef());
        }
    }

    @Test
    void exportsEmptyChildrenSheetWithoutTemplateExpressions() throws Exception {
        try (var workbook = export(List.of(employee("Alex Morgan", "alex.morgan@example.test")), List.of())) {
            var sheet = workbook.getSheet("Дети");
            assertEquals("Email сотрудника", sheet.getRow(2).getCell(1).getStringCellValue());
            for (var row : sheet) {
                for (var cell : row) {
                    assertFalse(cell.toString().contains("${"));
                }
            }
        }
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void exportsChildrenOnlyForSelectedEmployeesAndUsesProvidedAge(boolean includeFired) throws Exception {
        var employees = mock(AdminEmployeeService.class);
        var dictionaries = mock(DictService.class);
        var accounts = mock(BusinessAccountService.class);
        var exporter = mock(AdminEmployeeExcelExporter.class);
        var mapper = mock(EmployeeAllFieldsMapper.class);
        var clock = mock(DateTimeService.class);
        var auth = mock(AuthContext.class);
        var active = new EmployeeWithAllDetailsDto();
        active.setId(301);
        var fired = new EmployeeWithAllDetailsDto();
        fired.setId(501);
        when(employees.findAll(auth, includeFired)).thenReturn(includeFired ? Flux.just(active, fired) : Flux.just(active));
        when(mapper.toExportWithoutDictionaries(active)).thenReturn(employee("Alex Morgan", "alex.morgan@example.test"));
        when(mapper.toExportWithoutDictionaries(fired)).thenReturn(employee("Taylor Reed", "taylor.reed@example.test"));
        when(employees.findAllKids(auth)).thenReturn(Flux.just(kid(301, "Casey Morgan", LocalDate.of(2018, 10, 6), 8),
                kid(301, "Jordan Morgan", null, null), kid(501, "Sam Reed", LocalDate.of(2020, 11, 1), 5)));
        when(clock.now()).thenReturn(now);
        when(dictionaries.findProjects(auth)).thenReturn(Flux.empty());
        when(dictionaries.findDepartments(auth)).thenReturn(Flux.empty());
        when(dictionaries.findLevels(auth)).thenReturn(Flux.empty());
        when(dictionaries.findOfficeLocations(auth)).thenReturn(Flux.empty());
        when(dictionaries.findPositions(auth)).thenReturn(Flux.empty());
        when(dictionaries.findOrganizations(auth)).thenReturn(Flux.empty());
        when(accounts.findAllAsSimpleDict(false)).thenReturn(Flux.empty());
        var service = new AdminEmployeeExportService(employees, dictionaries, accounts, exporter, mapper, clock);
        StepVerifier.create(service.export(auth, EmployeeExportFilter.builder().includeFired(includeFired).build(), Locale.ENGLISH))
                .expectNextCount(1).verifyComplete();
        var captured = org.mockito.ArgumentCaptor.forClass(AdminEmployeeExcelExporter.AdminEmployeeExportBundle.class);
        verify(exporter).exportEmployees(captured.capture(), any());
        var bundle = captured.getValue();
        assertEquals(includeFired ? 2 : 1, bundle.getEmployees().size());
        assertEquals(includeFired ? 3 : 2, bundle.getKids().size());
        assertEquals(8, bundle.getKids().getFirst().getAge());
        assertNull(bundle.getKids().get(1).getAge());
        assertEquals("alex.morgan@example.test", bundle.getKids().getFirst().getParent().getEmail());
    }

    private XSSFWorkbook export(List<EmployeeExportDto> employees, List<EmployeeKidExportDto> kids) throws Exception {
        var exporter = new AdminEmployeeExcelExporter(new I18Helper.DummyI18Helper());
        exporter.setTemplate(new ClassPathResource("jxls/admin_employees_template.xlsx"));
        var bundle = AdminEmployeeExcelExporter.AdminEmployeeExportBundle.builder().employees(employees).kids(kids)
                .exportTime(now).locale(Locale.ENGLISH).build();
        try (var out = new ByteArrayOutputStream()) {
            exporter.exportEmployees(bundle, out);
            return new XSSFWorkbook(new ByteArrayInputStream(out.toByteArray()));
        }
    }

    private EmployeeExportDto employee(String name, String email) {
        var employee = new EmployeeExportDto();
        employee.setDisplayName(name);
        employee.setEmail(email);
        return employee;
    }

    private EmployeeKidDto kid(int parent, String name, LocalDate birthday, Integer age) {
        var kid = new EmployeeKidDto();
        kid.setParent(new SimpleDictDto(parent, "Example parent"));
        kid.setDisplayName(name);
        kid.setBirthday(birthday);
        kid.setAge(age);
        return kid;
    }
}
