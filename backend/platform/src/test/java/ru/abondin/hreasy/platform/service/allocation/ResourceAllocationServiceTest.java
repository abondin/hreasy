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
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.PeriodResourceAllocationView;
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
import static ru.abondin.hreasy.platform.service.allocation.ResourceAllocationSecurityValidator.EDIT_GLOBALLY_PERMISSION;
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
        when(repository.findClosedPeriods(anyInt())).thenReturn(Flux.empty());
        when(dateTimeService.now()).thenReturn(OffsetDateTime.parse("2026-09-03T10:00:00Z"));
    }

    @Test
    void loadsZeroBasedJanuaryAndSeparatesManagedProjectsFromEffectiveAccess() {
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 1, 31)))
                .thenReturn(Flux.just(employee(1)));
        when(repository.findProjects()).thenReturn(Flux.just(project(10), project(20)));
        when(repository.findAllocations(2026, 202600)).thenReturn(Flux.empty());
        when(repository.findAllocations(2025, 202511)).thenReturn(Flux.just(new ResourceAllocationView(1, 10, 40, 4)));
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
    void loadsCalendarYearForTheFirstManagedProject() {
        auth = new AuthContext("manager", "manager@example.test", List.of(EDIT_PERMISSION, EDIT_GLOBALLY_PERMISSION),
                new AuthContext.EmployeeInfo(1, null, null, List.of(), List.of(), List.of(), null, null));
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(employee(1), employee(2)));
        when(repository.findProjects()).thenReturn(Flux.just(project(20), project(10)));
        when(managerRepo.findManagedProjectHierarchyIds(1)).thenReturn(Flux.just(20));
        when(repository.findAllocatedProjectIds(2026)).thenReturn(Flux.empty());
        when(repository.findClosedPeriods(2026)).thenReturn(Flux.just(202601));
        when(repository.findProjectAllocations(20, 2026)).thenReturn(Flux.just(
                new PeriodResourceAllocationView(202605, 1, 20, 30, 4),
                new PeriodResourceAllocationView(202611, 2, 20, 50, 5)));

        StepVerifier.create(service.getProjectInput(2026, null, auth))
                .assertNext(input -> {
                    assertEquals(20, input.selectedProjectId());
                    assertEquals(12, input.months().size());
                    assertTrue(input.months().get(1).closed());
                    assertEquals(List.of("Project 10", "Project 20"),
                            input.projects().stream().map(project -> project.name()).toList());
                    assertEquals(2, input.allocations().size());
                })
                .verifyComplete();
    }

    @Test
    void loadsOnlyEmployeesAndProjectsWithAllocationsForAnnualAnalytics() {
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(employee(1), employee(2)));
        when(repository.findProjects()).thenReturn(Flux.just(project(10), project(20)));
        when(repository.findYearAllocations(2026)).thenReturn(Flux.just(
                new PeriodResourceAllocationView(202600, 1, 10, 40, 4),
                new PeriodResourceAllocationView(202601, 1, 10, 60, 5)));
        when(managerRepo.findManagedProjectHierarchyIds(1)).thenReturn(Flux.just(10));

        StepVerifier.create(service.getAnalytics(2026, auth))
                .assertNext(analytics -> {
                    assertEquals(List.of(1), analytics.employees().stream().map(employee -> employee.id()).toList());
                    assertEquals(List.of(10), analytics.projects().stream().map(project -> project.id()).toList());
                    assertEquals(List.of(202600, 202601),
                            analytics.allocations().stream().map(allocation -> allocation.period()).toList());
                })
                .verifyComplete();
    }

    @Test
    void savesOnlyChangedCellsInOneRevision() {
        var now = OffsetDateTime.parse("2026-08-27T10:00:00Z");
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(employee(1), employee(2)));
        when(repository.findProjects()).thenReturn(Flux.just(project(10)));
        when(repository.findProjectAllocations(10, 2026)).thenReturn(Flux.just(
                new PeriodResourceAllocationView(202600, 1, 10, 50, 5)));
        when(managerRepo.findManagedProjectHierarchyIds(1)).thenReturn(Flux.just(10));
        when(dateTimeService.now()).thenReturn(now);
        when(repository.createRevision(2026, 10, now, 1)).thenReturn(Mono.just(7));
        when(repository.recordChange(7, 202601, 2, 0, 70)).thenReturn(Mono.just(1L));
        when(repository.insertIfAbsent(2026, 202601, 2, 10, 70, 7)).thenReturn(Mono.just(1L));

        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(202600, 1, 50, 5),
                new ResourceAllocationSaveBody.Change(202601, 2, 70, null)));

        StepVerifier.create(service.save(2026, 10, request, auth))
                .expectNext(7)
                .verifyComplete();
        verify(repository).recordChange(7, 202601, 2, 0, 70);
        verify(repository).insertIfAbsent(2026, 202601, 2, 10, 70, 7);
    }

    @Test
    void rejectsChangesForInaccessibleProjects() {
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(employee(1)));
        when(repository.findProjects()).thenReturn(Flux.just(project(20)));
        when(repository.findProjectAllocations(20, 2026)).thenReturn(Flux.empty());
        when(managerRepo.findManagedProjectHierarchyIds(1)).thenReturn(Flux.just(20));

        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(202600, 1, 50, null)));

        StepVerifier.create(service.save(2026, 20, request, auth))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    void rejectsClosedMonthsAndMonthsAfterDismissal() {
        var dismissed = new ResourceAllocationEmployeeView(1, "Test Employee 1",
                null, null, 10, "Project 10", LocalDate.of(2020, 1, 1), LocalDate.of(2026, 8, 10));
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(dismissed));
        when(repository.findProjects()).thenReturn(Flux.just(project(10)));
        when(repository.findProjectAllocations(10, 2026)).thenReturn(Flux.empty());
        when(managerRepo.findManagedProjectHierarchyIds(1)).thenReturn(Flux.just(10));

        StepVerifier.create(service.save(2026, 10, new ResourceAllocationSaveBody(List.of(
                        new ResourceAllocationSaveBody.Change(202608, 1, 50, null))), auth))
                .expectErrorMatches(error -> error instanceof ru.abondin.hreasy.platform.BusinessError businessError
                        && "errors.resource_allocation.employee_not_employed".equals(businessError.getCode()))
                .verify();

        when(repository.findClosedPeriods(2026)).thenReturn(Flux.just(202607));
        StepVerifier.create(service.save(2026, 10, new ResourceAllocationSaveBody(List.of(
                        new ResourceAllocationSaveBody.Change(202607, 1, 50, null))), auth))
                .expectErrorMatches(error -> error instanceof ru.abondin.hreasy.platform.BusinessError businessError
                        && "errors.resource_allocation.period_closed".equals(businessError.getCode()))
                .verify();
    }

    @Test
    void returnsServerValuesAndNonConflictingDraftWhenRevisionChanged() {
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(employee(1), employee(2)));
        when(repository.findProjects()).thenReturn(Flux.just(project(10)));
        when(repository.findProjectAllocations(10, 2026)).thenReturn(Flux.just(
                new PeriodResourceAllocationView(202600, 1, 10, 80, 6),
                new PeriodResourceAllocationView(202601, 2, 10, 20, 5)));
        when(managerRepo.findManagedProjectHierarchyIds(1)).thenReturn(Flux.just(10));

        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(202600, 1, 60, 5),
                new ResourceAllocationSaveBody.Change(202601, 2, 40, 5)));

        StepVerifier.create(service.save(2026, 10, request, auth))
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
                null, null, null, null, LocalDate.of(2020, 1, 1), null);
    }

    private ResourceAllocationProjectView project(int id) {
        return new ResourceAllocationProjectView(id, "Project " + id,
                null, null, null, null, null, null);
    }
}
