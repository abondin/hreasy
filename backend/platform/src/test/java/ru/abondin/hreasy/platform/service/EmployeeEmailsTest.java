package ru.abondin.hreasy.platform.service;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.abondin.hreasy.platform.TestEmployees;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.admin.employee.AdminEmployeeService;
import ru.abondin.hreasy.platform.service.admin.employee.dto.CreateOrUpdateEmployeeBody;
import ru.abondin.hreasy.platform.service.admin.employee.dto.EmployeeAllFieldsMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = PostgreSQLTestContainerContextInitializer.class)
class EmployeeEmailsTest extends BaseServiceTest {
    @Autowired
    private AdminEmployeeService adminService;
    @Autowired
    private EmployeeService employeeService;
    @Autowired
    private EmployeeAllFieldsMapper mapper;
    @Autowired
    private DatabaseClient db;

    private int firstEmployee;
    private int secondEmployee;

    @BeforeEach
    void setUp() {
        initEmployeesDataAndLogin();
        firstEmployee = testData.employees.get(TestEmployees.Admin_Shaan_Pitts);
        secondEmployee = testData.employees.get(TestEmployees.FMS_Empl_Ammara_Knott);
    }

    @AfterEach
    void cleanUp() {
        db.sql("update empl.employee set additional_emails = '{}' where id in (:first, :second)")
                .bind("first", firstEmployee).bind("second", secondEmployee).then().block(MONO_DEFAULT_TIMEOUT);
    }

    @Test
    void savesAndRemovesAddressesInAdminDirectoryProfileAndHistory() {
        var body = body(firstEmployee);
        var primaryEmail = body.getEmail();
        body.setAdditionalEmails(List.of("New.Name@example.test", "other@example.test"));
        adminService.update(auth, firstEmployee, body).block(MONO_DEFAULT_TIMEOUT);

        var saved = adminService.get(auth, firstEmployee).block(MONO_DEFAULT_TIMEOUT);
        assertEquals(primaryEmail, saved.getEmail());
        assertEquals(List.of("new.name@example.test", "other@example.test"), saved.getAdditionalEmails());
        assertArrayEquals(new String[]{"new.name@example.test", "other@example.test"},
                employeeService.find(firstEmployee, auth).block(MONO_DEFAULT_TIMEOUT).getAdditionalEmails());
        assertArrayEquals(new String[]{"new.name@example.test", "other@example.test"},
                employeeService.findAll(auth, true).filter(e -> e.getId().equals(firstEmployee))
                        .single().block(MONO_DEFAULT_TIMEOUT).getAdditionalEmails());
        var historyEmails = db.sql("select additional_emails from empl.employee_history where employee = :id order by id desc limit 1")
                .bind("id", firstEmployee).map(row -> row.get("additional_emails", String[].class)).one().block(MONO_DEFAULT_TIMEOUT);
        assertArrayEquals(new String[]{"new.name@example.test", "other@example.test"}, historyEmails);

        // The import committer uses this same mapping when updating an existing employee.
        var importedBody = mapper.createOrUpdateBodyFromEntry(saved);
        adminService.update(auth, firstEmployee, importedBody).block(MONO_DEFAULT_TIMEOUT);
        assertEquals(saved.getAdditionalEmails(), body(firstEmployee).getAdditionalEmails());

        importedBody.setAdditionalEmails(List.of());
        adminService.update(auth, firstEmployee, importedBody).block(MONO_DEFAULT_TIMEOUT);
        assertEquals(List.of(), body(firstEmployee).getAdditionalEmails());
    }

    @Test
    void apiAllowsAdditionalAddressesSharedWithOtherEmployeesAndPrimaryAddresses() {
        var first = body(firstEmployee);
        var second = body(secondEmployee);
        first.setAdditionalEmails(List.of("shared@example.test", second.getEmail()));
        adminService.update(auth, firstEmployee, first).block(MONO_DEFAULT_TIMEOUT);
        second.setAdditionalEmails(List.of("shared@example.test", first.getEmail(), "shared@example.test"));
        adminService.update(auth, secondEmployee, second).block(MONO_DEFAULT_TIMEOUT);
        assertEquals(List.of("shared@example.test", second.getEmail().toLowerCase(java.util.Locale.ROOT)),
                body(firstEmployee).getAdditionalEmails());
        assertEquals(List.of("shared@example.test", first.getEmail().toLowerCase(java.util.Locale.ROOT), "shared@example.test"),
                body(secondEmployee).getAdditionalEmails());
    }

    private CreateOrUpdateEmployeeBody body(int employeeId) {
        return mapper.createOrUpdateBodyFromEntry(adminService.get(auth, employeeId).block(MONO_DEFAULT_TIMEOUT));
    }
}
