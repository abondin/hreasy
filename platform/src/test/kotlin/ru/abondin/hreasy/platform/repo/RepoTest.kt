package ru.abondin.hreasy.platform.repo

import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.data.domain.Sort
import org.springframework.data.r2dbc.core.isEquals
import org.springframework.data.r2dbc.query.Criteria
import org.springframework.test.context.ActiveProfiles
import org.springframework.test.context.ContextConfiguration
import reactor.test.StepVerifier

@ActiveProfiles("test", "dev")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = [SqlServerContextInitializer::class])
class RepoTest {

    @Autowired
    private lateinit var testDataGenerator: TestDataGenerator;
    @Autowired
    private lateinit var employeeRepo: EmployeeRepo

    @BeforeEach
    fun generateData() {
        testDataGenerator.generate().block();
    }

    @Test
    fun testFindAllEmployees() {
        /* We have 6 employees in database*/
        StepVerifier
                .create(employeeRepo.findDetailed(null, Sort.sort(EmployeeDetailedEntry::class.java).by(EmployeeDetailedEntry::lastname)))
                .expectNextCount(6)
                .verifyComplete();
    }

    @Test
    fun testFindTesterEmployees() {
        // We have only one Manual Tester - Chack
        val criteria = Criteria.where("departmentName").isEquals("QA");
        StepVerifier.create(employeeRepo.findDetailed(criteria, Sort.sort(EmployeeDetailedEntry::class.java).by(EmployeeDetailedEntry::lastname)))
                .expectNextMatches { e -> e.lastname.equals("Chack") }
                .verifyComplete();
        ;
    }


}