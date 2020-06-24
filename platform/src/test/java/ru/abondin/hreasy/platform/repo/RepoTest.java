package ru.abondin.hreasy.platform.repo;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Sort;
import org.springframework.data.relational.core.query.Criteria;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.repo.employee.EmployeeDetailedEntry;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;

import java.time.Duration;

// Test Data Generation is broken now
@Disabled
@ActiveProfiles({"test", "dev"})
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = {SqlServerContextInitializer.class})
public class RepoTest {

    @Autowired
    private TestDataGenerator testDataGenerator;
    @Autowired
    private EmployeeRepo employeeRepo;

    @BeforeEach
    protected void generateData() {
        testDataGenerator.generate().block(Duration.ofSeconds(10));
    }

    @Test
    public void testFindAllEmployees() {
        /* We have 6 employees in database*/
        StepVerifier
                .create(employeeRepo.findDetailed(null, Sort.sort(EmployeeDetailedEntry.class).by(EmployeeDetailedEntry::getLastname)))
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    public void testFindTesterEmployees() {
        // We have only one Manual Tester - Chack
        var criteria = Criteria.where("departmentName").is("QA");
        StepVerifier.create(employeeRepo.findDetailed(criteria, Sort.sort(EmployeeDetailedEntry.class).by(EmployeeDetailedEntry::getLastname)))
                .expectNextMatches(e -> e.getLastname().equals("Chack"))
                .verifyComplete();
        ;
    }

}
