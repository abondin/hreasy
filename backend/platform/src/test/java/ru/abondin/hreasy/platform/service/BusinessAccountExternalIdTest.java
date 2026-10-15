package ru.abondin.hreasy.platform.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.r2dbc.core.DatabaseClient;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.service.admin.ba.AdminBusinessAccountService;
import ru.abondin.hreasy.platform.service.admin.ba.dto.CreateOrUpdateBABody;
import ru.abondin.hreasy.platform.service.ba.BusinessAccountService;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = PostgreSQLTestContainerContextInitializer.class)
class BusinessAccountExternalIdTest extends BaseServiceTest {
    @Autowired
    private AdminBusinessAccountService adminService;
    @Autowired
    private BusinessAccountService service;
    @Autowired
    private DatabaseClient db;

    @BeforeEach
    void initialize() {
        initEmployeesDataAndLogin();
    }

    @Test
    void persistsKeyAndHistoryAndClearsNullOrEmptyValues() {
        var body = new CreateOrUpdateBABody();
        body.setName("Example business account");
        var key = "account-" + UUID.randomUUID();
        body.setExternalId(key);
        var id = adminService.create(auth, body).block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(id);
        assertEquals(key, service.get(id).block(MONO_DEFAULT_TIMEOUT).getExternalId());
        assertEquals(key, db.sql("select external_id from ba.business_account_history where ba_id = :id order by id desc limit 1")
                .bind("id", id).map((row, metadata) -> row.get("external_id", String.class))
                .one().block(MONO_DEFAULT_TIMEOUT));

        body.setExternalId(null);
        body.setName("Updated account name");
        adminService.update(auth, id, body).block(MONO_DEFAULT_TIMEOUT);
        var updated = service.get(id).block(MONO_DEFAULT_TIMEOUT);
        assertNull(updated.getExternalId());
        assertEquals("Updated account name", updated.getName());

        body.setExternalId(key);
        adminService.update(auth, id, body).block(MONO_DEFAULT_TIMEOUT);
        body.setExternalId("");
        adminService.update(auth, id, body).block(MONO_DEFAULT_TIMEOUT);
        assertNull(service.get(id).block(MONO_DEFAULT_TIMEOUT).getExternalId());
    }
}
