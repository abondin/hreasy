package ru.abondin.hreasy.platform.service.allocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationEmployeeView;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationProjectView;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationView;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSaveBody;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.abondin.hreasy.platform.service.allocation.ResourceAllocationSecurityValidator.EDIT_PERMISSION;

class ResourceAllocationServiceTest {
    private ResourceAllocationRepository repository;
    private DateTimeService dateTimeService;
    private ResourceAllocationService service;
    private AuthContext auth;

    @BeforeEach
    void setUp() {
        repository = mock(ResourceAllocationRepository.class);
        dateTimeService = mock(DateTimeService.class);
        service = new ResourceAllocationService(repository,
                new ResourceAllocationSecurityValidator(new ProjectHierarchyAccessor(null, null)),
                dateTimeService);
        auth = new AuthContext("manager", "manager@example.test", List.of(EDIT_PERMISSION),
                new AuthContext.EmployeeInfo(1, null, null, List.of(), List.of(), List.of(10), null, null));
    }

    @Test
    void loadsZeroBasedJanuaryAndMarksOnlyAccessibleProjectsEditable() {
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))
                .thenReturn(Flux.just(employee(1)));
        when(repository.findProjects()).thenReturn(Flux.just(project(10), project(20)));
        when(repository.findAllocations(202600)).thenReturn(Flux.empty());

        StepVerifier.create(service.getSheet(202600, auth))
                .assertNext(sheet -> {
                    assertTrue(sheet.projects().get(0).editable());
                    assertFalse(sheet.projects().get(1).editable());
                })
                .verifyComplete();
    }

    @Test
    void savesOnlyChangedCellsInOneRevision() {
        var now = OffsetDateTime.parse("2026-08-27T10:00:00Z");
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))
                .thenReturn(Flux.just(employee(1), employee(2)));
        when(repository.findProjects()).thenReturn(Flux.just(project(10)));
        when(repository.findAllocations(202600)).thenReturn(Flux.just(new ResourceAllocationView(1, 10, 50, 5)));
        when(dateTimeService.now()).thenReturn(now);
        when(repository.createRevision(202600, now, 1)).thenReturn(Mono.just(7));
        when(repository.recordChange(7, 2, 10, 0, 70)).thenReturn(Mono.just(1L));
        when(repository.upsert(202600, 2, 10, 70, 7)).thenReturn(Mono.just(1L));

        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(1, 10, 50),
                new ResourceAllocationSaveBody.Change(2, 10, 70)));

        StepVerifier.create(service.save(202600, request, auth))
                .expectNext(7)
                .verifyComplete();
        verify(repository).recordChange(7, 2, 10, 0, 70);
        verify(repository).upsert(202600, 2, 10, 70, 7);
    }

    @Test
    void rejectsChangesForInaccessibleProjects() {
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))
                .thenReturn(Flux.just(employee(1)));
        when(repository.findProjects()).thenReturn(Flux.just(project(20)));
        when(repository.findAllocations(202600)).thenReturn(Flux.empty());

        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(1, 20, 50)));

        StepVerifier.create(service.save(202600, request, auth))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    private ResourceAllocationEmployeeView employee(int id) {
        return new ResourceAllocationEmployeeView(id, "Test Employee " + id,
                null, null, null, null);
    }

    private ResourceAllocationProjectView project(int id) {
        return new ResourceAllocationProjectView(id, "Project " + id,
                null, null, null, null, null, null);
    }
}
