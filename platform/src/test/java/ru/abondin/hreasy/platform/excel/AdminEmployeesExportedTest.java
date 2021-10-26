package ru.abondin.hreasy.platform.excel;

import lombok.extern.slf4j.Slf4j;
import org.apache.logging.log4j.util.Strings;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ClassPathResource;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeExcelExporter;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeExportDto;

import java.io.FileOutputStream;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Slf4j
public class AdminEmployeesExportedTest {

    private final List<String> firstNames = Arrays.asList(
            "Иван", "Василий", "Александр", "Сергей", "Валерий", "Никита"
    );
    private final List<String> lastNames = Arrays.asList(
            "Чайкин", "Снигерёв", "Уткин", "Гусев", "Синицин", "Лебедев"
    );
    private final List<String> patronicNames = Arrays.asList(
            null, "Васильевич", "Витальевич", "Владимирович", "Сергеевич", "Александрович"
    );

    private final List<String> projects = Arrays.asList(
            "Facebook",
            "VK",
            "Одноклассники"
    );

    private final List<String> positions = Arrays.asList(
            "Разработчик первой категории",
            "Ведущий Бизнес Аналитик",
            "Дизайнер"
    );

    private final List<String> locations = Arrays.asList(
            "Комната 301",
            "307",
            "302 - Дубний"
    );

    private final List<String> departments = Arrays.asList(
            "Отдел Разработки",
            "Отдел Тестирования"
    );

    private final List<String> levels = Arrays.asList(
            "Junior",
            "Junior +",
            "Middle",
            "Senior"
    );

    private AdminEmployeeExcelExporter.AdminEmployeeExportBundle bundle;


    private AdminEmployeeExcelExporter exporter;

    @BeforeEach
    public void before() {
        var employees = new ArrayList<EmployeeExportDto>();
        int size = 100;
        var currentYear = LocalDate.now().getYear();
        for (int i = 1; i <= size; i++) {
            var lastname = lastNames.get((int) (Math.random() * lastNames.size()));
            var firstame = firstNames.get((int) (Math.random() * firstNames.size()));
            var patronicname = patronicNames.get((int) (Math.random() * patronicNames.size()));
            var displayName = Strings.join(Arrays.asList(lastname, firstame, patronicname), ' ');

            var employee = new EmployeeExportDto();
            employee.setDisplayName(displayName);
            employee.setBirthday(LocalDate.of(randomInt(1970, 2000), randomInt(1, 12), randomInt(1, 30)));
            employee.setChildren(Math.random() > 0.5 ? "Ребетёнок Маленький " + lastname : null);
            employee.setDateOfEmployment(LocalDate.of(randomInt(2014, 2021), randomInt(1, 12), randomInt(1, 30)));
            employee.setEmail(lastname + "." + firstame + "@company.ru");
            employee.setCityOfResidence(Math.random() > 0.5 ? "Бор" : "");
            employee.setCurrentProject(randomElement(projects));
            employee.setDateOfDismissal(Math.random() > 0.5 ? LocalDate.of(randomInt(2015, 2021), randomInt(1, 12), randomInt(1, 30)) : null);
            employee.setDepartment(randomElement(departments));
            employee.setDocumentFull("99 88 776655 УВД города Звёздный");
            employee.setEnglishLevel("Отличный");
            employee.setForeignPassport(Math.random() > 0.5 ? "Паспорт заграничный" : null);
            employee.setOfficeLocation(randomElement(locations));
            employee.setLevel(randomElement(levels));
            employee.setPhone("+79998885544");
            employee.setPosition(randomElement(positions));
            employee.setRegistrationAddress("Мой город не дом и не улица");
            employee.setSex(Math.random() > 0.5 ? "Муж" : "Жен");
            employee.setFamilyStatus(Math.random() > 0.5 ? "Женат" : "Замужем");
            employee.setSkype("skype_" + lastname);
            employee.setSpouseName("Супруга " + lastname);
            employee.setWorkDay("Полный");
            employee.setWorkType("Основной");
            employees.add(employee);
        }
        this.bundle = AdminEmployeeExcelExporter.AdminEmployeeExportBundle.builder()
                .exportTime(OffsetDateTime.now())
                .employees(employees)
                .build();

        exporter = new AdminEmployeeExcelExporter(new ClassPathResource("jxls/admin_employees_template.xlsx"));
    }


    @Test
    @Disabled("Nothing to tests yet. It is only to play with apache poi")
    public void testEmployeesExcelGeneration() throws Exception {
        log.info("Export test employees set to target/employees_out.xlsx");
        try (var fileOut = new FileOutputStream("target/employees_out.xlsx")) {
            exporter.exportEmployees(bundle, fileOut);
        }

    }

    private int randomInt(int from, int to) {
        return (int) (from + (to - from) * Math.random());
    }

    private String randomElement(List<String> elements) {
        return elements.get((int) (Math.random() * elements.size()));
    }

}
