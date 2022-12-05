package ru.abondin.hreasy.platform.excel;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.jxls.reader.*;
import org.springframework.core.io.ClassPathResource;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeExcelImporter;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeImportConfig;

import java.util.ArrayList;
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
        var status = reader.read(
                new ClassPathResource("excel/import_test_data.xlsx")
                        .getInputStream(), beans
        );
        Assertions.assertEquals(4, employees.size());
    }

    @Test
    public void testExcelImportRuntimeTemplate() throws Exception {
        var beans = new HashMap<String, Object>();
        var employees = new ArrayList<ImportExcelTestDataDto>();
        beans.put("employees", employees);
        var status = importer.configureReader(new EmployeeImportConfig()).read(
                new ClassPathResource("excel/employees-to-import.xlsx")
                        .getInputStream(), beans
        );
        Assertions.assertEquals(4, employees.size());
    }
}
