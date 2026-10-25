package ru.abondin.hreasy.platform.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.admin.dict.AdminDictOfficeService;
import ru.abondin.hreasy.platform.service.admin.dict.dto.CreateOrUpdateOfficeBody;
import ru.abondin.hreasy.platform.service.dict.DictService;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = PostgreSQLTestContainerContextInitializer.class)
class PrimaryOfficeTest extends BaseServiceTest {
    @Autowired
    private AdminDictOfficeService adminService;
    @Autowired
    private DictService dictService;

    @Test
    void persistsPrimaryFlagAndExcludesArchivedOfficesFromPublicDictionary() {
        initEmployeesDataAndLogin();
        var body = new CreateOrUpdateOfficeBody();
        body.setName("Primary Office Test A");
        var id = adminService.create(auth, body).block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(id);
        StepVerifier.create(dictService.findOffices(auth).filter(office -> office.getId() == id))
                .assertNext(office -> assertFalse(office.isPrimaryOffice())).verifyComplete();

        body.setPrimaryOffice(true);
        StepVerifier.create(adminService.update(auth, id, body)).expectNext(id).verifyComplete();
        StepVerifier.create(dictService.findOffices(auth).filter(office -> office.getId() == id))
                .assertNext(office -> assertTrue(office.isPrimaryOffice())).verifyComplete();
        StepVerifier.create(adminService.findAll(auth).filter(office -> office.getId() == id))
                .assertNext(office -> assertTrue(office.isPrimaryOffice())).verifyComplete();

        body.setName("Primary Office Test B");
        var secondId = adminService.create(auth, body).block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(secondId);
        StepVerifier.create(dictService.findOffices(auth).filter(office -> office.getId() == secondId))
                .assertNext(office -> assertTrue(office.isPrimaryOffice())).verifyComplete();

        body.setPrimaryOffice(false);
        StepVerifier.create(adminService.update(auth, secondId, body)).expectNext(secondId).verifyComplete();
        StepVerifier.create(dictService.findOffices(auth).filter(office -> office.getId() == secondId))
                .assertNext(office -> assertFalse(office.isPrimaryOffice())).verifyComplete();

        body.setPrimaryOffice(true);
        body.setArchived(true);
        StepVerifier.create(adminService.update(auth, secondId, body)).expectNext(secondId).verifyComplete();
        StepVerifier.create(dictService.findOffices(auth).filter(office -> office.getId() == secondId))
                .verifyComplete();
    }
}
