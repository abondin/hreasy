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
        body.setWorkstreams(List.of(new ProjectDto.WorkstreamBody(null, "delivery", "Delivery", null, false)));
        var projectId = admin.create(auth, body).block(MONO_DEFAULT_TIMEOUT);
        var entry = workstreams.findActiveByProjectId(projectId).single().block(MONO_DEFAULT_TIMEOUT);
        var id = entry.getId();
        assertNull(entry.getArchivedAt());

        body.setWorkstreams(List.of(new ProjectDto.WorkstreamBody(id, "delivery", "Delivery", null, true)));
        admin.update(auth, projectId, body).block(MONO_DEFAULT_TIMEOUT);
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

        admin.update(auth, projectId, body).block(MONO_DEFAULT_TIMEOUT);
        assertEquals(archivedAt, workstreams.findById(id).block(MONO_DEFAULT_TIMEOUT).getArchivedAt());
        body.setWorkstreams(List.of(new ProjectDto.WorkstreamBody(id, "delivery", "Delivery", null, false)));
        admin.update(auth, projectId, body).block(MONO_DEFAULT_TIMEOUT);
        entry = workstreams.findById(id).block(MONO_DEFAULT_TIMEOUT);
        assertNull(entry.getArchivedAt());
        assertNull(entry.getArchivedBy());

        var snapshots = history.findAll().filter(h -> "project_workstream".equals(h.getEntityType()) && id.equals(h.getEntityId()))
                .sort(java.util.Comparator.comparing(h -> h.getId())).collectList().block(MONO_DEFAULT_TIMEOUT);
        assertEquals(3, snapshots.size());
        assertTrue(snapshots.stream().allMatch(h -> h.getCreatedBy().equals(auth.getEmployeeInfo().getEmployeeId())));
        assertFalse(json.readTree(snapshots.get(1).getEntityValue().asString()).get("archivedAt").isNull());
        assertTrue(json.readTree(snapshots.get(2).getEntityValue().asString()).get("archivedAt").isNull());
    }
}
