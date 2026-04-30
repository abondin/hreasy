package ru.abondin.hreasy.platform.excel;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jxls.reader.ReaderBuilder;
import org.springframework.core.io.ClassPathResource;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.service.admin.employee.imp.AdminEmployeeExcelImporter;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.EmployeeImportConfig;
import ru.abondin.hreasy.platform.service.admin.employee.imp.dto.ImportEmployeeExcelRowDto;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;

@Slf4j
public class AdminEmployeesImporterTest {
    private AdminEmployeeExcelImporter importer;

    @BeforeEach
    public void before() {
        importer = new AdminEmployeeExcelImporter();
    }

    @Test
    public void testExcelImportXMLTemplate() throws Exception {
        var reader = ReaderBuilder.buildFromXML(new ClassPathResource("excel/example-exel-data.xml")
                .getInputStream());
        var beans = new HashMap<String, Object>();
        var employees = new ArrayList<ImportExcelTestDataDto>();
        beans.put("employees", employees);
        reader.read(
                new ClassPathResource("excel/import_test_data.xlsx")
                        .getInputStream(), beans
        );
        Assertions.assertEquals(4, employees.size());
        Assertions.assertEquals(new Date(74, 0, 2), employees.get(0).getBirthday());
        Assertions.assertEquals(new Date(77, 8, 26), employees.get(1).getBirthday());
    }

    @Test
    public void testExcelImportRuntimeTemplate() throws Exception {
        StepVerifier.create(importer.importFromFile(new EmployeeImportConfig(), new ClassPathResource("excel/employees-to-import.xlsx")
                        .getInputStream()))
                .assertNext((empl) -> {
                    assertEqualsStr("Новый Сотрудник Сотрудникович", empl.getDisplayName());
                    assertEqualsStr("СТЗК-00000", empl.getExternalErpId());
                    Assertions.assertEquals("new.employee@example.test", empl.getEmail());
                    assertEqualsStr("79998887766", empl.getPhone());
                    assertEqualsStr("Основное", empl.getDepartment());
                    assertEqualsStr("Ведущий менеджер проекта", empl.getPosition());
                    assertEqualsStr("11.11.2025", empl.getDateOfEmployment());
                    Assertions.assertNull(empl.getDateOfDismissal().getRaw());
                    assertEqualsStr("01.01.1980", empl.getBirthday());
                    assertEqualsStr("Мужской", empl.getSex());
                    assertEqualsStr("11 22", empl.getDocumentSeries());
                    assertEqualsStr("334455", empl.getDocumentNumber());
                    assertEqualsStr("01.02.2003", empl.getDocumentIssuedDate());
                    assertEqualsStr("Test Authority", empl.getDocumentIssuedBy());
                    assertEqualsStr("Test address", empl.getRegistrationAddress());
                    Assertions.assertNull(empl.getOrganization().getRaw());
                })
                .verifyComplete();
    }

    private void assertEqualsStr(String expected, ImportEmployeeExcelRowDto.DataProperty<?> property) {
        Assertions.assertNotNull(property.getRaw(), "Expected value " + expected + " but not null");
        Assertions.assertEquals(expected, property.getRaw().trim());
    }
}
