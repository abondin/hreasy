package ru.abondin.hreasy.platform.excel;

import org.apache.poi.ss.usermodel.CellType;
import org.apache.poi.ss.usermodel.DataFormatter;
import org.apache.poi.ss.util.CellRangeAddress;
import org.apache.poi.xssf.usermodel.XSSFSheet;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.springframework.context.support.ReloadableResourceBundleMessageSource;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.Default18Helper;
import ru.abondin.hreasy.platform.service.excel.ExcelReportFactory;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeExcelExporter;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeExportService;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.*;
import ru.abondin.hreasy.platform.service.ba.BusinessAccountService;
import ru.abondin.hreasy.platform.service.dict.DictService;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.dto.OfficeDictDto;
import ru.abondin.hreasy.platform.service.dto.OfficeLocationDictDto;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
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
            assertEquals(28, employees.getRow(2).getLastCellNum());
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
            assertEquals("A3:AB5", employees.getTables().getFirst().getArea().formatAsString());
            assertEquals("A3:AB5", employees.getTables().getFirst().getCTTable().getAutoFilter().getRef());
            assertEquals("A3:E6", children.getTables().getFirst().getArea().formatAsString());
            assertEquals("A3:E6", children.getTables().getFirst().getCTTable().getAutoFilter().getRef());
            for (var sheet : List.of(employees, children)) {
                assertNotNull(sheet.getDrawingPatriarch());
                assertEquals(1, sheet.getDrawingPatriarch().getShapes().size());
            }
            assertEquals(List.of("B1:AB1", "C2:AB2"),
                    employees.getMergedRegions().stream().map(CellRangeAddress::formatAsString).toList());
        }
    }

    @Test
    void exportsEmptyTablesWithHeadersAndFilters() throws Exception {
        try (var workbook = export(List.of(), List.of())) {
            var sheet = workbook.getSheet("Дети");
            assertEquals("Email сотрудника", sheet.getRow(2).getCell(1).getStringCellValue());
            for (var emptySheet : workbook) {
                var table = ((XSSFSheet) emptySheet).getTables().getFirst();
                assertEquals(3, table.getEndRowIndex());
                assertEquals(table.getArea().formatAsString(), table.getCTTable().getAutoFilter().getRef());
            }
        }
    }

    @ParameterizedTest
    @CsvSource({"false,true,,,1", "true,true,,,2", "true,false,,,2",
            "true,true,101,,1", "true,true,,201,1", "true,true,101,202,0"})
    void filtersEmployeesAndTheirChildren(boolean includeFired, boolean includeKids,
                                         Integer department, Integer ba, int expectedCount) throws Exception {
        var employees = mock(AdminEmployeeService.class);
        var dictionaries = mock(DictService.class);
        var accounts = mock(BusinessAccountService.class);
        var exporter = mock(AdminEmployeeExcelExporter.class);
        var mapper = mock(EmployeeAllFieldsMapper.class);
        var clock = mock(DateTimeService.class);
        var auth = mock(AuthContext.class);
        var active = new EmployeeWithAllDetailsDto();
        active.setId(301);
        active.setDepartmentId(101);
        active.setBaId(201);
        active.setOfficeLocationId(501);
        var fired = new EmployeeWithAllDetailsDto();
        fired.setId(501);
        fired.setDepartmentId(102);
        fired.setBaId(202);
        when(employees.findAll(auth, includeFired)).thenReturn(includeFired ? Flux.just(active, fired) : Flux.just(active));
        var activeExport = employee("Alex Morgan", "alex.morgan@example.test");
        when(mapper.toExportWithoutDictionaries(active)).thenReturn(activeExport);
        when(mapper.toExportWithoutDictionaries(fired)).thenReturn(employee("Taylor Reed", "taylor.reed@example.test"));
        when(employees.findAllKids(auth)).thenReturn(Flux.just(kid(301, "Casey Morgan", LocalDate.of(2018, 10, 6), 8),
                kid(301, "Jordan Morgan", null, null), kid(501, "Sam Reed", LocalDate.of(2020, 11, 1), 5)));
        when(clock.now()).thenReturn(now);
        when(dictionaries.findProjects(auth)).thenReturn(Flux.empty());
        when(dictionaries.findDepartments(auth)).thenReturn(Flux.empty());
        when(dictionaries.findLevels(auth)).thenReturn(Flux.empty());
        when(dictionaries.findOfficeLocations(auth)).thenReturn(Flux.just(
                new OfficeLocationDictDto(501, "Example room", 601, null)));
        var office = new OfficeDictDto();
        office.setId(601);
        office.setName("Example office");
        when(dictionaries.findOffices(auth)).thenReturn(Flux.just(office));
        when(dictionaries.findPositions(auth)).thenReturn(Flux.empty());
        when(dictionaries.findOrganizations(auth)).thenReturn(Flux.empty());
        when(accounts.findAllAsSimpleDict(false)).thenReturn(Flux.empty());
        var service = new AdminEmployeeExportService(employees, dictionaries, accounts, exporter, mapper, clock);
        var filter = EmployeeExportFilter.builder().includeFired(includeFired).includeKids(includeKids)
                .departmentIds(department == null ? List.of() : List.of(999, department))
                .baIds(ba == null ? List.of() : List.of(999, ba)).build();
        StepVerifier.create(service.export(auth, filter, Locale.ENGLISH))
                .expectNextCount(1).verifyComplete();
        var captured = org.mockito.ArgumentCaptor.forClass(AdminEmployeeExcelExporter.AdminEmployeeExportBundle.class);
        verify(exporter).exportEmployees(captured.capture(), any());
        var bundle = captured.getValue();
        assertEquals(expectedCount, bundle.getEmployees().size());
        if (expectedCount == 0) {
            assertTrue(bundle.getKids().isEmpty());
            return;
        }
        assertEquals("Example office", activeExport.getOffice());
        assertEquals("Example room", activeExport.getOfficeLocation());
        if (!includeKids) {
            assertTrue(bundle.getKids().isEmpty());
            verify(employees, never()).findAllKids(auth);
            return;
        }
        assertEquals(expectedCount == 2 ? 3 : 2, bundle.getKids().size());
        assertEquals(8, bundle.getKids().getFirst().getAge());
        assertNull(bundle.getKids().get(1).getAge());
        assertEquals("alex.morgan@example.test", bundle.getKids().getFirst().getParent().getEmail());
    }

    private XSSFWorkbook export(List<EmployeeExportDto> employees, List<EmployeeKidExportDto> kids) throws Exception {
        return export(employees, kids, EmployeeExportFilter.builder()
                .includeAdditional(true).includePassport(true).includeKids(true).build());
    }

    @ParameterizedTest
    @CsvSource({"false,false,false", "true,false,false", "false,true,false", "false,false,true"})
    void exportsOnlySelectedColumnsAndPreservesFullBirthday(boolean additional, boolean passport, boolean kids) throws Exception {
        var filter = EmployeeExportFilter.builder().includeAdditional(additional)
                .includePassport(passport).includeKids(kids).build();
        var employee = employee("Alex Morgan", "alex.morgan@example.test");
        employee.setBirthday(LocalDate.of(1990, 4, 15));
        employee.setPhone("test-phone");
        employee.setDocumentFull("test-passport");
        employee.setChildren("test-children");
        try (var workbook = export(List.of(employee), List.of(), filter)) {
            assertEquals(filter.isIncludeKids() ? 2 : 1, workbook.getNumberOfSheets());
            var sheet = workbook.getSheet("Сотрудники");
            var headers = new ArrayList<String>();
            sheet.getRow(2).forEach(cell -> headers.add(cell.getStringCellValue()));
            assertEquals(filter.isIncludeAdditional(), headers.contains("Телефон"));
            assertEquals(filter.isIncludeAdditional(), headers.contains("Дата увольнения"));
            assertEquals(filter.isIncludePassport(), headers.contains("Документ УЛ"));
            assertEquals(filter.isIncludePassport(), headers.contains("Загранпаспорт"));
            assertEquals(filter.isIncludeKids(), headers.contains("Дети"));
            assertEquals(List.of("ФИО", "Email", "Подразделение", "Бизнес аккаунт", "Текущий проект",
                    "Роль на проекте", "Позиция", "Офис", "Рабочее место"), headers.subList(0, 9));
            for (var header : List.of("Telegram", "Пол", "День рождения", "Кабинет")) {
                assertEquals(filter.isIncludeAdditional(), headers.contains(header));
            }
            assertEquals(9 + (filter.isIncludeAdditional() ? 15 : 0) + (filter.isIncludePassport() ? 3 : 0)
                    + (filter.isIncludeKids() ? 1 : 0), headers.size());
            if (filter.isIncludeAdditional()) {
                var birthday = sheet.getRow(3).getCell(headers.indexOf("День рождения"));
                assertEquals(employee.getBirthday(), birthday.getLocalDateTimeCellValue().toLocalDate());
                assertEquals("15.04.1990", new DataFormatter(Locale.ENGLISH).formatCellValue(birthday));
            }
            var strings = workbook.getSharedStringSource().getSharedStringItems().stream().map(Object::toString).toList();
            assertEquals(filter.isIncludeAdditional(), strings.contains("test-phone"));
            assertEquals(filter.isIncludePassport(), strings.contains("test-passport"));
            assertEquals(filter.isIncludeKids(), strings.contains("test-children"));
        }
    }

    private XSSFWorkbook export(List<EmployeeExportDto> employees, List<EmployeeKidExportDto> kids,
                                EmployeeExportFilter filter) throws Exception {
        return export(employees, kids, filter, Locale.forLanguageTag("ru-RU"));
    }

    @Test
    void localizesWorkbookTextUsingExportLocale() throws Exception {
        var filter = EmployeeExportFilter.builder().includeAdditional(true).includePassport(true).includeKids(true).build();
        try (var workbook = export(List.of(), List.of(), filter, Locale.ENGLISH)) {
            var employees = workbook.getSheet("Employees");
            assertNotNull(employees);
            assertNotNull(workbook.getSheet("Children"));
            assertEquals("Employee export from HR Easy", employees.getRow(0).getCell(1).getStringCellValue());
            assertEquals("Exported at", employees.getRow(1).getCell(1).getStringCellValue());
            assertEquals("Full name", employees.getRow(2).getCell(0).getStringCellValue());
            assertEquals("Employee email", workbook.getSheet("Children").getRow(2).getCell(1).getStringCellValue());
        }
    }

    private XSSFWorkbook export(List<EmployeeExportDto> employees, List<EmployeeKidExportDto> kids,
                                EmployeeExportFilter filter, Locale locale) throws Exception {
        var messages = new ReloadableResourceBundleMessageSource();
        messages.setBasename("classpath:i18n/messages");
        messages.setDefaultEncoding("UTF-8");
        messages.setFallbackToSystemLocale(false);
        var i18n = new Default18Helper(messages);
        var exporter = new AdminEmployeeExcelExporter(i18n, new ExcelReportFactory(i18n));
        var bundle = AdminEmployeeExcelExporter.AdminEmployeeExportBundle.builder().employees(employees).kids(kids)
                .exportTime(now).locale(locale).filter(filter).build();
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
