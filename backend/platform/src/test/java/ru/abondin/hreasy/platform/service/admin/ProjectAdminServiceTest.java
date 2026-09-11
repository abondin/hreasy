package ru.abondin.hreasy.platform.service.admin;

import org.junit.jupiter.api.Test;
import org.reactivestreams.Publisher;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;
import ru.abondin.hreasy.platform.repo.dict.DictProjectHistoryRepo;
import ru.abondin.hreasy.platform.repo.dict.DictProjectRepo;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamEntry;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamRepo;
import ru.abondin.hreasy.platform.repo.employee.admin.SecAdminUserRolesRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDto;
import ru.abondin.hreasy.platform.service.admin.dto.ProjectDtoMapper;

import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

/** Repository mocks are intentional: this test verifies soft-delete marking before entries are passed to saveAll. */
class ProjectAdminServiceTest {

    @Test
    @SuppressWarnings("unchecked")
    void softDeletesWorkstreamsRemovedFromProjectUpdate() {
        var projectRepo = mock(DictProjectRepo.class);
        var historyRepo = mock(DictProjectHistoryRepo.class);
        var dateTimeService = mock(DateTimeService.class);
        var securityValidator = mock(AdminSecurityValidator.class);
        var mapper = mock(ProjectDtoMapper.class);
        var adminUserRolesRepo = mock(SecAdminUserRolesRepo.class);
        var workstreamRepo = mock(ProjectWorkstreamRepo.class);
        var service = new ProjectAdminService(projectRepo, historyRepo, dateTimeService, securityValidator,
                mapper, adminUserRolesRepo, workstreamRepo);
        var now = OffsetDateTime.parse("2026-09-07T08:00:00Z");
        var project = new DictProjectEntry();
        project.setId(10);
        var history = new DictProjectEntry.ProjectHistoryEntry(10, "Project", 1);
        var workstream = new ProjectWorkstreamEntry();
        workstream.setId(20);
        workstream.setProjectId(10);
        workstream.setDisplayName("Delivery");
        var body = new ProjectDto.CreateOrUpdateProjectDto();
        body.setName("Project");
        body.setDepartmentId(1);
        body.setWorkstreams(List.of());
        var auth = new AuthContext("admin", "admin@example.test", List.of(),
                new AuthContext.EmployeeInfo(1, null, null, List.of(), List.of(), List.of(), null, null));

        when(dateTimeService.now()).thenReturn(now);
        when(projectRepo.findById(10)).thenReturn(Mono.just(project));
        when(securityValidator.validateUpdateProject(auth, project)).thenReturn(Mono.just(true));
        when(mapper.historyEntry(1, now, project)).thenReturn(history);
        when(historyRepo.save(history)).thenReturn(Mono.just(history));
        when(projectRepo.save(project)).thenReturn(Mono.just(project));
        when(workstreamRepo.findActiveByProjectId(10)).thenReturn(Flux.just(workstream));
        when(workstreamRepo.saveAll(any(Publisher.class))).thenAnswer(invocation ->
                Flux.from((Publisher<ProjectWorkstreamEntry>) invocation.getArgument(0)));

        StepVerifier.create(service.update(auth, 10, body)).expectNext(10).verifyComplete();

        assertEquals(now, workstream.getDeletedAt());
        assertEquals(1, workstream.getDeletedBy());
    }
}
