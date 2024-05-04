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
        StepVerifier.create(importer.importFromFile(employeesToImportConfig(), new ClassPathResource("excel/employees-to-import.xlsx")
                        .getInputStream()))
                .assertNext((empl) -> {
                    // Хайден Спуннер
                    assertEqualsStr("Хайден Спуннер", empl.getDisplayName());
                    assertEqualsStr("СТЗК-00111", empl.getExternalErpId());
                    Assertions.assertEquals("Haiden.Spooner@stm-labs.ru", empl.getEmail());
                    assertEqualsStr("79998884455", empl.getPhone());
                    assertEqualsStr("Development", empl.getDepartment());
                    assertEqualsStr("Java Developer", empl.getPosition());
                    assertEqualsStr("01.10.2020", empl.getDateOfEmployment());
                    assertEqualsStr("01.10.2022", empl.getDateOfDismissal());
                    assertEqualsStr("14.07.1995", empl.getBirthday());
                    assertEqualsStr("Мужской", empl.getSex());
                    assertEqualsStr("45 65", empl.getDocumentSeries());
                    assertEqualsStr("123456", empl.getDocumentNumber());
                    assertEqualsStr("13.08.2019", empl.getDocumentIssuedDate());
                    assertEqualsStr("ГУ МВД России", empl.getDocumentIssuedBy());
                    assertEqualsStr("РОССИЯ, 602222, Лучший город на свете, супер улица", empl.getRegistrationAddress());
                    assertEqualsStr("STM", empl.getOrganization());
                })
                .assertNext((empl) -> {
                    // Кнотт Амара Юрьевна
                    assertEqualsStr("Кнотт Амара Юрьевна", empl.getDisplayName());
                    assertEqualsStr("СТЗК-00112", empl.getExternalErpId());
                    Assertions.assertEquals("Ammara.Knott@stm-labs.ru", empl.getEmail());
                    Assertions.assertNull(empl.getPhone().getRaw());
                    assertEqualsStr("Непонятный отдел", empl.getDepartment());
                    assertEqualsStr("Project Manager", empl.getPosition());
                    assertEqualsStr("20.06.2022", empl.getDateOfEmployment());
                    Assertions.assertNull(empl.getDateOfDismissal().getRaw());
                    assertEqualsStr("01.02.1998", empl.getBirthday());
                    assertEqualsStr("женский", empl.getSex());
                    assertEqualsStr("87 23", empl.getDocumentSeries());
                    assertEqualsStr("859173", empl.getDocumentNumber());
                    assertEqualsStr("14.02.2018", empl.getDocumentIssuedDate());
                    assertEqualsStr("ГУ МВД России по Нижегородской области", empl.getDocumentIssuedBy());
                    Assertions.assertNull(empl.getRegistrationAddress().getRaw());
                    Assertions.assertNull(empl.getOrganization().getRaw());
                })
                .assertNext((empl) -> {
                    // Бобов Асиях Петрович
                    assertEqualsStr("Бобов Асиях Петрович", empl.getDisplayName());
                    assertEqualsStr("СТЗК-00113", empl.getExternalErpId());
                    Assertions.assertEquals("Asiyah.Bob@stm-labs.ru", empl.getEmail());
                    assertEqualsStr("79998884455", empl.getPhone());
                    assertEqualsStr("Integration", empl.getDepartment());
                    assertEqualsStr("Непонятная должность", empl.getPosition());
                    assertEqualsStr("28.03.2022", empl.getDateOfEmployment());
                    Assertions.assertNull(empl.getDateOfDismissal().getRaw());
                    assertEqualsStr("06.08.1993", empl.getBirthday());
                    assertEqualsStr("Муж.", empl.getSex());
                    assertEqualsStr("87 21", empl.getDocumentSeries());
                    assertEqualsStr("563732", empl.getDocumentNumber());
                    assertEqualsStr("20.09.2022", empl.getDocumentIssuedDate());
                    assertEqualsStr("ГУ МВД России по Нижегородской области", empl.getDocumentIssuedBy());
                    assertEqualsStr("РОССИЯ, 602222, Лучший город на свете, супер улица", empl.getRegistrationAddress());
                    assertEqualsStr("STM Labs", empl.getOrganization());
                })
                .verifyComplete();
    }

    private EmployeeImportConfig employeesToImportConfig() {
        var config = new EmployeeImportConfig();
        config.getColumns().setEmail("Q");
        config.getColumns().setPhone("AG");
        config.getColumns().setDepartment("AH");
        config.getColumns().setPosition("AI");
        config.getColumns().setDateOfEmployment("AJ");
        config.getColumns().setDateOfDismissal("AK");
        config.getColumns().setBirthday("AM");
        config.getColumns().setSex("AN");
        config.getColumns().setDocumentSeries("AP");
        config.getColumns().setDocumentNumber("AQ");
        config.getColumns().setDocumentIssuedDate("AR");
        config.getColumns().setDocumentIssuedBy("AS");
        config.getColumns().setRegistrationAddress("AU");
        config.getColumns().setOrganization("BA");
        return config;
    }


    private void assertEqualsStr(String expected, ImportEmployeeExcelRowDto.DataProperty<?> property) {
        Assertions.assertNotNull(property.getRaw(), "Expected value "+expected+" but not null");
        Assertions.assertEquals(expected, property.getRaw().trim());
    }
}
