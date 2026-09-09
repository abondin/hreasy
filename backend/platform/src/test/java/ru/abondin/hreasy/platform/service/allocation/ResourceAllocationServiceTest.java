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
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamRepo;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamEntry;
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
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static ru.abondin.hreasy.platform.service.allocation.ResourceAllocationSecurityValidator.READ_PERMISSION;
import static ru.abondin.hreasy.platform.service.allocation.ResourceAllocationSecurityValidator.ADMIN_PERMISSION;
import static ru.abondin.hreasy.platform.service.allocation.ResourceAllocationSecurityValidator.WRITE_PERMISSION;

class ResourceAllocationServiceTest {
    private ResourceAllocationRepository repository;
    private DateTimeService dateTimeService;
    private ProjectWorkstreamRepo workstreamRepo;
    private ResourceAllocationService service;
    private AuthContext auth;

    @BeforeEach
    void setUp() {
        repository = mock(ResourceAllocationRepository.class);
        dateTimeService = mock(DateTimeService.class);
        workstreamRepo = mock(ProjectWorkstreamRepo.class);
        service = new ResourceAllocationService(repository,
                new ResourceAllocationSecurityValidator(new ProjectHierarchyAccessor(null, null)),
                dateTimeService, workstreamRepo);
        auth = new AuthContext("manager", "manager@example.test", List.of(READ_PERMISSION, WRITE_PERMISSION),
                new AuthContext.EmployeeInfo(1, null, null, List.of(), List.of(), List.of(10), null, null));
        when(repository.lockPeriod(anyInt())).thenReturn(Mono.empty());
        when(repository.findClosedPeriods(anyInt())).thenReturn(Flux.empty());
        when(workstreamRepo.findActiveByProjectId(anyInt())).thenReturn(Flux.empty());
        when(workstreamRepo.findAll()).thenReturn(Flux.empty());
        when(dateTimeService.now()).thenReturn(OffsetDateTime.parse("2026-09-03T10:00:00Z"));
    }

    @Test
    void loadsOnlyWritableProjectsAndDefaultsToFirstOne() {
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(employee(1), employee(2)));
        when(repository.findProjects()).thenReturn(Flux.just(project(20), project(10)));
        when(repository.findAllocatedProjectIds(2026)).thenReturn(Flux.empty());
        when(repository.findClosedPeriods(2026)).thenReturn(Flux.just(202601));
        when(repository.findProjectAllocations(10, null, 2026)).thenReturn(Flux.just(
                new PeriodResourceAllocationView(202605, 1, 10, null, 30, 4),
                new PeriodResourceAllocationView(202611, 2, 10, null, 50, 5)));
        when(repository.findOtherProjectAllocations(10, null, 2026)).thenReturn(Flux.just(
                new ResourceAllocationRepository.OtherProjectAllocationView(202605, 1, 40, false),
                new ResourceAllocationRepository.OtherProjectAllocationView(202606, 2, 30, true)));

        StepVerifier.create(service.getProjectInput(2026, null, null, auth))
                .assertNext(input -> {
                    assertEquals(10, input.selectedProjectId());
                    assertEquals(12, input.months().size());
                    assertTrue(input.months().get(1).closed());
                    assertEquals(List.of("Project 10"),
                            input.projects().stream().map(project -> project.name()).toList());
                    assertTrue(input.projects().getFirst().editable());
                    assertEquals("Developer", input.employees().getFirst().currentProjectRole());
                    assertEquals("employee1@example.test", input.employees().getFirst().email());
                    assertEquals(2, input.allocations().size());
                    assertEquals(40, input.otherAllocations().getFirst().percent());
                    assertTrue(input.otherAllocations().getLast().sameProject());
                })
                .verifyComplete();
    }

    @Test
    void filtersProjectsByYearButKeepsProjectsWithAllocations() {
        auth.getEmployeeInfo().setAccessibleProjects(List.of(10, 20, 30, 40));
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.empty());
        when(repository.findProjects()).thenReturn(Flux.just(
                project(10, LocalDate.of(2020, 1, 1), LocalDate.of(2025, 12, 31)),
                project(20, LocalDate.of(2027, 1, 1), null),
                project(30, LocalDate.of(2020, 1, 1), LocalDate.of(2025, 12, 31)),
                project(40, LocalDate.of(2020, 1, 1), LocalDate.of(2026, 6, 30))));
        when(repository.findAllocatedProjectIds(2026)).thenReturn(Flux.just(30));
        when(repository.findProjectAllocations(30, null, 2026)).thenReturn(Flux.empty());
        when(repository.findOtherProjectAllocations(30, null, 2026)).thenReturn(Flux.empty());

        StepVerifier.create(service.getProjectInput(2026, null, null, auth))
                .assertNext(input -> {
                    assertEquals(List.of(30, 40), input.projects().stream().map(project -> project.id()).toList());
                    assertEquals(LocalDate.of(2026, 6, 30), input.projects().getLast().endDate());
                })
                .verifyComplete();
    }

    @Test
    void loadsOnlyEmployeesAndProjectsWithAllocationsForAnnualAnalytics() {
        auth.setAuthorities(List.of(READ_PERMISSION));
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(employee(1), employee(2)));
        when(repository.findProjects()).thenReturn(Flux.just(project(10), project(20)));
        when(repository.findYearAllocations(2026)).thenReturn(Flux.just(
                new PeriodResourceAllocationView(202600, 1, 10, null, 40, 4),
                new PeriodResourceAllocationView(202601, 1, 10, null, 60, 5)));

        StepVerifier.create(service.getAnalytics(2026, auth))
                .assertNext(analytics -> {
                    assertEquals(List.of(1), analytics.employees().stream().map(employee -> employee.id()).toList());
                    assertEquals("Developer", analytics.employees().getFirst().currentProjectRole());
                    assertEquals("employee1@example.test", analytics.employees().getFirst().email());
                    assertEquals(List.of(10), analytics.projects().stream().map(project -> project.id()).toList());
                    assertEquals(List.of(202600, 202601),
                            analytics.allocations().stream().map(allocation -> allocation.period()).toList());
                })
                .verifyComplete();
    }

    @Test
    void savesOnlyChangedPeriodStatesWithAdminPermission() {
        auth.setAuthorities(List.of(READ_PERMISSION, WRITE_PERMISSION, ADMIN_PERMISSION));
        var now = OffsetDateTime.parse("2026-09-03T10:00:00Z");
        when(dateTimeService.now()).thenReturn(now);
        when(repository.findClosedPeriods(2026)).thenReturn(Flux.just(202600, 202602));
        when(repository.closePeriod(2026, 202601, now, 1)).thenReturn(Mono.just(1L));
        when(repository.reopenPeriod(202602, now, 1)).thenReturn(Mono.just(1L));

        StepVerifier.create(service.saveClosedPeriods(2026, List.of(202600, 202601), auth))
                .expectNext(List.of(202600, 202601))
                .verifyComplete();
        verify(repository, never()).closePeriod(2026, 202600, now, 1);
        verify(repository).closePeriod(2026, 202601, now, 1);
        verify(repository).reopenPeriod(202602, now, 1);

        auth.setAuthorities(List.of(READ_PERMISSION, WRITE_PERMISSION));
        StepVerifier.create(service.saveClosedPeriods(2026, List.of(), auth))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    void savesOnlyChangedCellsInOneRevision() {
        var now = OffsetDateTime.parse("2026-08-27T10:00:00Z");
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(employee(1), employee(2)));
        when(repository.findProjects()).thenReturn(Flux.just(project(10)));
        when(repository.findProjectAllocations(10, null, 2026)).thenReturn(Flux.just(
                new PeriodResourceAllocationView(202600, 1, 10, null, 50, 5)));
        when(dateTimeService.now()).thenReturn(now);
        when(repository.createRevision(2026, 10, null, now, 1)).thenReturn(Mono.just(7));
        when(repository.recordChange(7, 202601, 2, null, 70)).thenReturn(Mono.just(1L));
        when(repository.insertIfAbsent(2026, 202601, 2, 10, null, 70, 7)).thenReturn(Mono.just(1L));

        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(202600, 1, 50, 5),
                new ResourceAllocationSaveBody.Change(202601, 2, 70, null)));

        StepVerifier.create(service.save(2026, 10, null, request, auth))
                .expectNext(7)
                .verifyComplete();
        verify(repository).recordChange(7, 202601, 2, null, 70);
        verify(repository).insertIfAbsent(2026, 202601, 2, 10, null, 70, 7);
    }

    @Test
    void savesAnIndependentWorkstreamAllocation() {
        var now = OffsetDateTime.parse("2026-08-27T10:00:00Z");
        var workstream = new ProjectWorkstreamEntry();
        workstream.setId(30);
        workstream.setProjectId(10);
        when(workstreamRepo.findById(30)).thenReturn(Mono.just(workstream));
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(employee(1)));
        when(repository.findProjects()).thenReturn(Flux.just(project(10)));
        when(repository.findProjectAllocations(10, 30, 2026)).thenReturn(Flux.empty());
        when(dateTimeService.now()).thenReturn(now);
        when(repository.createRevision(2026, 10, 30, now, 1)).thenReturn(Mono.just(8));
        when(repository.recordChange(8, 202601, 1, null, 60)).thenReturn(Mono.just(1L));
        when(repository.insertIfAbsent(2026, 202601, 1, 10, 30, 60, 8)).thenReturn(Mono.just(1L));

        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(202601, 1, 60, null)));

        StepVerifier.create(service.save(2026, 10, 30, request, auth))
                .expectNext(8)
                .verifyComplete();
        verify(repository).insertIfAbsent(2026, 202601, 1, 10, 30, 60, 8);
    }

    @Test
    void rejectsChangesForInaccessibleProjects() {
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(employee(1)));
        when(repository.findProjects()).thenReturn(Flux.just(project(20)));
        when(repository.findProjectAllocations(20, null, 2026)).thenReturn(Flux.empty());

        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(202600, 1, 50, null)));

        StepVerifier.create(service.save(2026, 20, null, request, auth))
                .expectError(AccessDeniedException.class)
                .verify();
    }

    @Test
    void rejectsClosedMonthsAndMonthsAfterDismissal() {
        var dismissed = new ResourceAllocationEmployeeView(1, "Test Employee 1",
                null, null, 10, "Project 10", "Developer",
                LocalDate.of(2020, 1, 1), LocalDate.of(2026, 8, 10), "employee@example.test");
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(dismissed));
        when(repository.findProjects()).thenReturn(Flux.just(project(10)));
        when(repository.findProjectAllocations(10, null, 2026)).thenReturn(Flux.empty());

        StepVerifier.create(service.save(2026, 10, null, new ResourceAllocationSaveBody(List.of(
                        new ResourceAllocationSaveBody.Change(202608, 1, 50, null))), auth))
                .expectErrorMatches(error -> error instanceof ru.abondin.hreasy.platform.BusinessError businessError
                        && "errors.resource_allocation.employee_not_employed".equals(businessError.getCode()))
                .verify();

        when(repository.findClosedPeriods(2026)).thenReturn(Flux.just(202607));
        StepVerifier.create(service.save(2026, 10, null, new ResourceAllocationSaveBody(List.of(
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
        when(repository.findProjectAllocations(10, null, 2026)).thenReturn(Flux.just(
                new PeriodResourceAllocationView(202600, 1, 10, null, 80, 6),
                new PeriodResourceAllocationView(202601, 2, 10, null, 20, 5)));

        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(202600, 1, 60, 5),
                new ResourceAllocationSaveBody.Change(202601, 2, 40, 5)));

        StepVerifier.create(service.save(2026, 10, null, request, auth))
                .expectErrorSatisfies(error -> {
                    var conflict = (ru.abondin.hreasy.platform.BusinessError) error;
                    assertEquals(HttpStatus.CONFLICT, conflict.getStatus());
                    assertEquals(2, ((List<?>) conflict.getAttrs().get("allocations")).size());
                    assertEquals(1, ((List<?>) conflict.getAttrs().get("changes")).size());
                    assertEquals(1, ((List<?>) conflict.getAttrs().get("conflicts")).size());
                })
                .verify();
    }

    @Test
    void distinguishesNewZeroUpdatedZeroAndDeletionAfterDismissal() {
        var now = dateTimeService.now();
        var dismissed = new ResourceAllocationEmployeeView(1, "Alex Morgan",
                null, null, 10, "Project 10", "Developer",
                LocalDate.of(2020, 1, 1), LocalDate.of(2026, 8, 10), "alex.morgan@example.test");
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.just(dismissed));
        when(repository.findProjects()).thenReturn(Flux.just(project(10)));
        when(repository.findProjectAllocations(10, null, 2026)).thenReturn(Flux.just(
                new PeriodResourceAllocationView(202607, 1, 10, null, 50, 5),
                new PeriodResourceAllocationView(202608, 1, 10, null, 0, 6)));
        when(repository.createRevision(2026, 10, null, now, 1)).thenReturn(Mono.just(7));
        when(repository.recordChange(7, 202606, 1, null, 0)).thenReturn(Mono.just(1L));
        when(repository.insertIfAbsent(2026, 202606, 1, 10, null, 0, 7)).thenReturn(Mono.just(1L));
        when(repository.recordChange(7, 202607, 1, 50, 0)).thenReturn(Mono.just(1L));
        when(repository.updateIfRevisionMatches(2026, 202607, 1, 10, null, 0, 7, 5))
                .thenReturn(Mono.just(1L));
        when(repository.recordChange(7, 202608, 1, 0, null)).thenReturn(Mono.just(1L));
        when(repository.deleteIfRevisionMatches(2026, 202608, 1, 10, null, 6)).thenReturn(Mono.just(1L));
        var request = new ResourceAllocationSaveBody(List.of(
                new ResourceAllocationSaveBody.Change(202606, 1, 0, null),
                new ResourceAllocationSaveBody.Change(202607, 1, 0, 5),
                new ResourceAllocationSaveBody.Change(202608, 1, null, 6)));

        StepVerifier.create(service.save(2026, 10, null, request, auth)).expectNext(7).verifyComplete();
        verify(repository).recordChange(7, 202606, 1, null, 0);
        verify(repository).recordChange(7, 202607, 1, 50, 0);
        verify(repository).recordChange(7, 202608, 1, 0, null);
    }

    @Test
    void rejectsZeroAfterDismissalAndStillProtectsClosedPeriodsAndRevisionsOnClear() {
        when(repository.findEmployees(LocalDate.of(2026, 1, 1), LocalDate.of(2026, 12, 31)))
                .thenReturn(Flux.empty());
        when(repository.findProjects()).thenReturn(Flux.just(project(10)));
        when(repository.findProjectAllocations(10, null, 2026)).thenReturn(Flux.just(
                new PeriodResourceAllocationView(202600, 1, 10, null, 50, 5)));
        StepVerifier.create(service.save(2026, 10, null, new ResourceAllocationSaveBody(List.of(
                        new ResourceAllocationSaveBody.Change(202600, 1, 0, 5))), auth))
                .expectErrorMatches(error -> error instanceof ru.abondin.hreasy.platform.BusinessError be
                        && "errors.resource_allocation.employee_not_employed".equals(be.getCode()))
                .verify();
        when(repository.findClosedPeriods(2026)).thenReturn(Flux.just(202600));
        StepVerifier.create(service.save(2026, 10, null, new ResourceAllocationSaveBody(List.of(
                        new ResourceAllocationSaveBody.Change(202600, 1, null, 5))), auth))
                .expectErrorMatches(error -> error instanceof ru.abondin.hreasy.platform.BusinessError be
                        && "errors.resource_allocation.period_closed".equals(be.getCode()))
                .verify();
        when(repository.findClosedPeriods(2026)).thenReturn(Flux.empty());
        StepVerifier.create(service.save(2026, 10, null, new ResourceAllocationSaveBody(List.of(
                        new ResourceAllocationSaveBody.Change(202600, 1, null, 4))), auth))
                .expectErrorMatches(error -> error instanceof ru.abondin.hreasy.platform.BusinessError be
                        && be.getStatus() == HttpStatus.CONFLICT)
                .verify();
    }

    private ResourceAllocationEmployeeView employee(int id) {
        return new ResourceAllocationEmployeeView(id, "Test Employee " + id,
                null, null, null, null, "Developer", LocalDate.of(2020, 1, 1), null,
                "employee" + id + "@example.test");
    }

    private ResourceAllocationProjectView project(int id) {
        return project(id, null, null);
    }

    private ResourceAllocationProjectView project(int id, LocalDate startDate, LocalDate endDate) {
        return new ResourceAllocationProjectView(id, "Project " + id,
                null, null, null, null, startDate, endDate);
    }
}
