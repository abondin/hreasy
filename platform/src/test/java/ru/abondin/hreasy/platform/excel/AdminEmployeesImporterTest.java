package ru.abondin.hreasy.platform.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.jxls.reader.ReaderBuilder;
import org.springframework.core.io.ClassPathResource;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeExcelExporter;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeExcelImporter;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeExportDto;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

@Slf4j
public class AdminEmployeesImporterTest {
    private AdminEmployeeExcelImporter importer;

    @BeforeEach
    public void before() {
        importer = new AdminEmployeeExcelImporter(new ClassPathResource("jxls/import_employees_template.xml"));
    }


    @Test
    public void testExcelImport() throws Exception {
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
}
