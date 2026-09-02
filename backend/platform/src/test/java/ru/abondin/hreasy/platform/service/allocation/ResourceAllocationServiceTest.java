package ru.abondin.hreasy.platform.service.allocation;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.AccessDeniedException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationEmployeeView;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationProjectView;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationView;
import ru.abondin.hreasy.platform.repo.manager.ManagerRepo;
import ru.abondin.hreasy.platform.sec.ProjectHierarchyAccessor;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSaveBody;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.abondin.hreasy.platform.service.allocation.ResourceAllocationSecurityValidator.EDIT_PERMISSION;

class ResourceAllocationServiceTest {
    private ResourceAllocationRepository repository;
    private DateTimeService dateTimeService;
    private ManagerRepo managerRepo;
    private ResourceAllocationService service;
    private AuthContext auth;

    @BeforeEach
    void setUp() {
        repository = mock(ResourceAllocationRepository.class);
        dateTimeService = mock(DateTimeService.class);
        managerRepo = mock(ManagerRepo.class);
        service = new ResourceAllocationService(repository,
                new ResourceAllocationSecurityValidator(new ProjectHierarchyAccessor(null, null)),
                dateTimeService, managerRepo);
        auth = new AuthContext("manager", "manager@example.test", List.of(EDIT_PERMISSION),
                new AuthContext.EmployeeInfo(1, null, null, List.of(), List.of(), List.of(10), null, null));
        when(managerRepo.findManagedProjectHierarchyIds(1)).thenReturn(Flux.empty());
        when(repository.lockPeriod(anyInt())).thenReturn(Mono.empty());
    }

    @Test
    void loadsZeroBasedJanuaryAndSeparatesManagedProjectsFromEffectiveAccess() {
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))
                .thenReturn(Flux.just(employee(1)));
        when(repository.findProjects()).thenReturn(Flux.just(project(10), project(20)));
        when(repository.findAllocations(202600)).thenReturn(Flux.empty());
        when(repository.findAllocations(202511)).thenReturn(Flux.just(new ResourceAllocationView(1, 10, 40, 4)));
        when(managerRepo.findManagedProjectHierarchyIds(1)).thenReturn(Flux.just(20));

        StepVerifier.create(service.getSheet(202600, auth))
                .assertNext(sheet -> {
                    assertTrue(sheet.projects().get(0).editable());
                    assertFalse(sheet.projects().get(0).managed());
                    assertFalse(sheet.projects().get(1).editable());
                    assertTrue(sheet.projects().get(1).managed());
                    assertEquals(40, sheet.previousAllocations().getFirst().percent());
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
        when(repository.insertIfAbsent(202600, 2, 10, 70, 7)).thenReturn(Mono.just(1L));

        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(1, 10, 50, 5),
                new ResourceAllocationSaveBody.Change(2, 10, 70, null)));

        StepVerifier.create(service.save(202600, request, auth))
                .expectNext(7)
                .verifyComplete();
        verify(repository).recordChange(7, 2, 10, 0, 70);
        verify(repository).insertIfAbsent(202600, 2, 10, 70, 7);
    }

    @Test
    void rejectsChangesForInaccessibleProjects() {
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))
                .thenReturn(Flux.just(employee(1)));
        when(repository.findProjects()).thenReturn(Flux.just(project(20)));
        when(repository.findAllocations(202600)).thenReturn(Flux.empty());

        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(1, 20, 50, null)));

        StepVerifier.create(service.save(202600, request, auth))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    void returnsServerValuesAndNonConflictingDraftWhenRevisionChanged() {
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))
                .thenReturn(Flux.just(employee(1), employee(2)));
        when(repository.findProjects()).thenReturn(Flux.just(project(10)));
        when(repository.findAllocations(202600)).thenReturn(Flux.just(
                new ResourceAllocationView(1, 10, 80, 6),
                new ResourceAllocationView(2, 10, 20, 5)));

        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(1, 10, 60, 5),
                new ResourceAllocationSaveBody.Change(2, 10, 40, 5)));

        StepVerifier.create(service.save(202600, request, auth))
                .expectErrorSatisfies(error -> {
                    var conflict = (ru.abondin.hreasy.platform.BusinessError) error;
                    assertEquals(HttpStatus.CONFLICT, conflict.getStatus());
                    assertEquals(2, ((List<?>) conflict.getAttrs().get("allocations")).size());
                    assertEquals(1, ((List<?>) conflict.getAttrs().get("changes")).size());
                    assertEquals(1, ((List<?>) conflict.getAttrs().get("conflicts")).size());
                })
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
