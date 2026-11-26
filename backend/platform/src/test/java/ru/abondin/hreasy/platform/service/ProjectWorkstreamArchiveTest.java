package ru.abondin.hreasy.platform.service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import ru.abondin.hreasy.platform.repo.PostgreSQLTestContainerContextInitializer;
import ru.abondin.hreasy.platform.repo.dict.DepartmentRepo;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamRepo;
import ru.abondin.hreasy.platform.repo.history.HistoryRepo;
import ru.abondin.hreasy.platform.service.admin.ProjectAdminService;
import ru.abondin.hreasy.platform.service.admin.ProjectWorkstreamAdminService;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectWorkstreamBody;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDto;
import ru.abondin.hreasy.platform.service.dict.DictService;
import ru.abondin.hreasy.platform.service.external.ExternalApiService;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@ActiveProfiles("test")
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.MOCK)
@ContextConfiguration(initializers = PostgreSQLTestContainerContextInitializer.class)
class ProjectWorkstreamArchiveTest extends BaseServiceTest {
    @Autowired private ProjectAdminService admin;
    @Autowired private ProjectWorkstreamAdminService workstreamAdmin;
    @Autowired private ProjectWorkstreamRepo workstreams;
    @Autowired private DepartmentRepo departments;
    @Autowired private HistoryRepo history;
    @Autowired private DictService dictionaries;
    @Autowired private ExternalApiService external;
    @Autowired private ObjectMapper json;

    @Test
    void archivesAndRestoresWithoutLosingDictionaryEntriesOrAuditTrail() {
        initEmployeesDataAndLogin();
        var body = new ProjectDto.CreateOrUpdateProjectDto();
        body.setName("Archive lifecycle project");
        body.setExternalId("archive-lifecycle-project");
        body.setDepartmentId(departments.findAll().next().block(MONO_DEFAULT_TIMEOUT).getId());
        body.setWorkstreamRequired(false);
        var projectId = admin.create(auth, body).block(MONO_DEFAULT_TIMEOUT);
        workstreamAdmin.create(auth, projectId, new ProjectWorkstreamBody("delivery", "Delivery", null, false))
                .block(MONO_DEFAULT_TIMEOUT);
        var entry = workstreams.findActiveByProjectId(projectId).single().block(MONO_DEFAULT_TIMEOUT);
        var id = entry.getId();
        assertNull(entry.getArchivedAt());

        var archive = new ProjectWorkstreamBody("delivery", "Delivery", null, true);
        workstreamAdmin.update(auth, projectId, id, archive).block(MONO_DEFAULT_TIMEOUT);
        entry = workstreams.findById(id).block(MONO_DEFAULT_TIMEOUT);
        var archivedAt = entry.getArchivedAt();
        assertNotNull(archivedAt);
        assertEquals(auth.getEmployeeInfo().getEmployeeId(), entry.getArchivedBy());
        assertNull(entry.getDeletedAt());
        var dict = dictionaries.findProjects(auth).filter(p -> projectId.equals(p.getId())).single().block(MONO_DEFAULT_TIMEOUT);
        assertEquals(archivedAt, dict.getWorkstreams().getFirst().archivedAt());
        var api = external.projects(auth).filter(p -> "archive-lifecycle-project".equals(p.externalId()))
                .single().block(MONO_DEFAULT_TIMEOUT);
        assertEquals(archivedAt, api.workstreams().getFirst().archivedAt());

        workstreamAdmin.update(auth, projectId, id, archive).block(MONO_DEFAULT_TIMEOUT);
        body.setName("Updated project title");
        admin.update(auth, projectId, body).block(MONO_DEFAULT_TIMEOUT);
        assertEquals(archivedAt, workstreams.findById(id).block(MONO_DEFAULT_TIMEOUT).getArchivedAt());
        workstreamAdmin.update(auth, projectId, id, new ProjectWorkstreamBody("delivery", "Delivery", null, false))
                .block(MONO_DEFAULT_TIMEOUT);
        entry = workstreams.findById(id).block(MONO_DEFAULT_TIMEOUT);
        assertNull(entry.getArchivedAt());
        assertNull(entry.getArchivedBy());

        var snapshots = history.findAll().filter(h -> "project_workstream".equals(h.getEntityType()) && id.equals(h.getEntityId()))
                .sort(java.util.Comparator.comparing(h -> h.getId())).collectList().block(MONO_DEFAULT_TIMEOUT);
        assertEquals(3, snapshots.size());
        assertTrue(snapshots.stream().allMatch(h -> h.getCreatedBy().equals(auth.getEmployeeInfo().getEmployeeId())));
        assertFalse(json.readTree(snapshots.get(1).getEntityValue().asString()).get("archivedAt").isNull());
        assertTrue(json.readTree(snapshots.get(2).getEntityValue().asString()).get("archivedAt").isNull());

        body.setWorkstreamRequired(true);
        admin.update(auth, projectId, body).block(MONO_DEFAULT_TIMEOUT);
        var lastDirectionError = assertThrows(ru.abondin.hreasy.platform.BusinessError.class,
                () -> workstreamAdmin.delete(auth, projectId, id).block(MONO_DEFAULT_TIMEOUT));
        assertEquals("errors.project.workstream.required", lastDirectionError.getCode());
        assertNull(workstreams.findById(id).block(MONO_DEFAULT_TIMEOUT).getDeletedAt());

        var stranger = new ru.abondin.hreasy.platform.auth.AuthContext("stranger", "stranger@example.test", List.of(),
                new ru.abondin.hreasy.platform.auth.AuthContext.EmployeeInfo(-1, null, null, List.of(), List.of(), List.of(), null, null));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> workstreamAdmin.create(stranger, projectId, archive).block(MONO_DEFAULT_TIMEOUT));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> workstreamAdmin.update(stranger, projectId, id, archive).block(MONO_DEFAULT_TIMEOUT));
        assertThrows(org.springframework.security.access.AccessDeniedException.class,
                () -> workstreamAdmin.delete(stranger, projectId, id).block(MONO_DEFAULT_TIMEOUT));

        body.setWorkstreamRequired(false);
        body.setExternalId("another-archive-project");
        var otherProjectId = admin.create(auth, body).block(MONO_DEFAULT_TIMEOUT);
        assertThrows(ru.abondin.hreasy.platform.BusinessError.class,
                () -> workstreamAdmin.update(auth, otherProjectId, id, archive).block(MONO_DEFAULT_TIMEOUT));
        assertThrows(ru.abondin.hreasy.platform.BusinessError.class,
                () -> workstreamAdmin.delete(auth, otherProjectId, id).block(MONO_DEFAULT_TIMEOUT));
        body.setExternalId("archive-lifecycle-project");
        admin.update(auth, projectId, body).block(MONO_DEFAULT_TIMEOUT);
        workstreamAdmin.delete(auth, projectId, id).block(MONO_DEFAULT_TIMEOUT);
        entry = workstreams.findById(id).block(MONO_DEFAULT_TIMEOUT);
        assertNotNull(entry.getDeletedAt());
        assertEquals(auth.getEmployeeInfo().getEmployeeId(), entry.getDeletedBy());
        assertTrue(workstreamAdmin.findAll(auth, projectId).collectList().block(MONO_DEFAULT_TIMEOUT).isEmpty());
        body.setWorkstreamRequired(true);
        assertThrows(ru.abondin.hreasy.platform.BusinessError.class,
                () -> admin.update(auth, projectId, body).block(MONO_DEFAULT_TIMEOUT));
    }
}
