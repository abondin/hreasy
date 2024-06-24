package ru.abondin.hreasy.platform.tg;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.HrEasyTelegramBotProps;
import ru.abondin.hreasy.platform.service.EmployeeService;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.tg.dto.FindEmployeeRequest;
import ru.abondin.hreasy.platform.tg.dto.TgMapperImpl;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;

@Slf4j
public class FindEmployeeTest {

    private List<EmployeeDto> testEmployees = new ArrayList<>();
    private Locale locale;
    private AuthContext authContext;
    private TelegramFindEmployeesService findEmployeesService;

    @BeforeEach
    void setUp() {
        authContext = Mockito.mock(AuthContext.class);
        locale = Locale.forLanguageTag("ru");
        initializeTestEmployees();
        findEmployeesService = initializeService();
    }

    @Test
    void testFindByEmail() {
        var query = new FindEmployeeRequest("Petr.Ivanov@company.org", 5);
        var response = findEmployeesService.find(authContext, query, locale).block();
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertEquals(1, response.getEmployees().size(), "Expected 1 employee when find by email");
        Assertions.assertEquals("Иванов Петр Алексеевич", response.getEmployees().get(0).getDisplayName());
    }

    @Test
    void testFindExactMatchByFullName() {
        var query = new FindEmployeeRequest("Иванов Петр Алексеевич", 5);
        var response = findEmployeesService.find(authContext, query, locale).block();
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertEquals(1, response.getEmployees().size(), "Expected 1 employee for exact full name match");
        Assertions.assertEquals("Иванов Петр Алексеевич", response.getEmployees().get(0).getDisplayName());
    }

    @Test
    void testFindByLastName() {
        var query = new FindEmployeeRequest("Петров", 5);
        var response = findEmployeesService.find(authContext, query, locale).block();
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertEquals(1, response.getEmployees().size(), "Expected 1 employee for exact full name match");
        Assertions.assertEquals("Петров Иван Сидорович", response.getEmployees().get(0).getDisplayName());
    }


    @Test
    void testFindMultipleMatchesByLastName() {
        var query = new FindEmployeeRequest("Иванов", 5);
        var response = findEmployeesService.find(authContext, query, locale).block();
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertEquals(2, response.getEmployees().size(), "Expected 2 employees for last name match");
        Assertions.assertTrue(response.getEmployees().stream()
                .anyMatch(employee -> employee.getDisplayName().equals("Иванов Петр Алексеевич")));
        Assertions.assertTrue(response.getEmployees().stream()
                .anyMatch(employee -> employee.getDisplayName().equals("Иванов Петр Степанович")));
    }

    @Test
    void testFindByLastNameWithYe() {
        // Test case for finding employees with "ё" and "е" in the query
        var query = new FindEmployeeRequest("Пчелкина", 5); // Using "е" instead of "ё"
        var response = findEmployeesService.find(authContext, query, locale).block();
        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertEquals(1, response.getEmployees().size(), "Expected 1 employee for query with 'е' instead of 'ё'");
        Assertions.assertEquals("Пчёлкина Антонина Сергеевна", response.getEmployees().get(0).getDisplayName());
    }


    private TelegramFindEmployeesService initializeService() {
        var tgMapper = new TgMapperImpl();

        var employeeService = Mockito.mock(EmployeeService.class);
        Mockito.when(employeeService.findAll(Mockito.any(AuthContext.class), Mockito.eq(false)))
                .thenReturn(Flux.fromIterable(testEmployees));
        return new TelegramFindEmployeesService(employeeService, tgMapper, new HrEasyTelegramBotProps());
    }

    private void initializeTestEmployees() {
        // Test data for tests in Russian
        testEmployees.addAll(Arrays.asList(
                new EmployeeDto(1, "Иванов Петр Алексеевич", "male", null, null, null, null, null,
                        "Petr.Ivanov@company.org", "petro1999", null, false, new ArrayList<>()),
                new EmployeeDto(2, "Иванов Петр Степанович", "male", null, null, null, null, null,
                        "Petr.Ivanov2@company.org", "petro1999", null, false, new ArrayList<>()),
                new EmployeeDto(3, "Сидоров Иван Петрович", "male", null, null, null, null, null,
                        "Ivan.Sidorov@company.org", "ivan123", null, false, new ArrayList<>()),
                new EmployeeDto(4, "Петров Иван Сидорович", "male", null, null, null, null, null,
                        "Ivan.Petrov@company.org", "petrov", null, false, new ArrayList<>()),
                new EmployeeDto(4, "Пчёлкина Антонина Сергеевна", "female", null, null, null, null, null,
                        "Antonina.Pchelkina@company.org", "bee123", null, false, new ArrayList<>())
        ));
    }
}