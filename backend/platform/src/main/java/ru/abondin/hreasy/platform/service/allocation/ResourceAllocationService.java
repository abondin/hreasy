package ru.abondin.hreasy.platform.service.allocation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationEmployeeView;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.PeriodResourceAllocationView;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationProjectView;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamEntry;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSaveBody;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSaveBody.Change;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationProjectInputDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationProjectInputDto.MonthDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto.EmployeeDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto.ProjectDto;
import ru.abondin.hreasy.platform.service.dto.ProjectWorkstreamDto;

import java.time.DateTimeException;
import java.time.Year;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.IntStream;

/**
 * Builds annual allocation views and persists changed cells in immutable batch revisions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceAllocationService {
    private final ResourceAllocationRepository repository;
    private final ResourceAllocationSecurityValidator securityValidator;
    private final DateTimeService dateTimeService;
    private final ProjectWorkstreamRepo workstreamRepo;

    /**
     * Loads the allocation hierarchy, including explicit zero values, for one calendar year.
     */
    @Transactional(readOnly = true)
    public Mono<ResourceAllocationAnalyticsDto> getAnalytics(int year, AuthContext auth) {
        return securityValidator.validateCanReadAllocations(auth).then(Mono.defer(() -> {
            var selectedYear = parseYear(year);
            return Mono.zip(
                            repository.findEmployees(selectedYear.atDay(1),
                                    selectedYear.atMonth(12).atEndOfMonth()).collectList(),
                            repository.findProjects().collectList(),
                            repository.findYearAllocations(year).collectList(),
                            workstreamRepo.findAll().collectList())
                    .map(data -> {
                        var accessibleProjectIds = data.getT2().stream()
                                .filter(project -> securityValidator.canReadProject(auth, project))
                                .map(ResourceAllocationProjectView::id).collect(java.util.stream.Collectors.toSet());
                        var employeeIds = data.getT1().stream()
                                .filter(employee -> accessibleProjectIds.contains(employee.currentProjectId()))
                                .map(ResourceAllocationEmployeeView::id).collect(java.util.stream.Collectors.toSet());
                        data.getT3().stream().filter(allocation -> accessibleProjectIds.contains(allocation.projectId()))
                                .map(PeriodResourceAllocationView::employeeId).forEach(employeeIds::add);
                        var allocations = data.getT3().stream()
                                .filter(allocation -> employeeIds.contains(allocation.employeeId())).toList();
                        var projectIds = allocations.stream()
                                .map(PeriodResourceAllocationView::projectId).collect(java.util.stream.Collectors.toSet());
                        var allocatedEmployeeIds = allocations.stream()
                                .map(PeriodResourceAllocationView::employeeId).collect(java.util.stream.Collectors.toSet());
                        var workstreamIds = allocations.stream()
                                .map(PeriodResourceAllocationView::workstreamId).filter(Objects::nonNull)
                                .collect(java.util.stream.Collectors.toSet());
                        return new ResourceAllocationAnalyticsDto(year,
                                data.getT1().stream().filter(employee -> allocatedEmployeeIds.contains(employee.id()))
                                        .map(this::toEmployeeDto).toList(),
                                data.getT2().stream().filter(project -> projectIds.contains(project.id()))
                                        .map(project -> toProjectDto(project, selectedYear, auth))
                                        .sorted(Comparator.comparing(ProjectDto::name)).toList(),
                                data.getT4().stream().filter(workstream -> workstreamIds.contains(workstream.getId()))
                                        .map(this::toWorkstreamDto).toList(),
                                allocations.stream().map(allocation ->
                                        new ResourceAllocationAnalyticsDto.AllocationDto(allocation.period(),
                                                allocation.employeeId(), allocation.projectId(),
                                                allocation.workstreamId(), allocation.percent()))
                                        .toList());
                    });
        }));
    }

    /**
     * Loads all twelve months of one calendar year for a single project.
     */
    @Transactional(readOnly = true)
    public Mono<ResourceAllocationProjectInputDto> getProjectInput(int year, Integer requestedProjectId,
                                                                   Integer requestedWorkstreamId,
                                                                   AuthContext auth) {
        return securityValidator.validateCanReadAllocations(auth).then(Mono.defer(() -> {
            var selectedYear = parseYear(year);
            var yearStart = selectedYear.atDay(1);
            var yearEnd = selectedYear.atMonth(12).atEndOfMonth();
            return Mono.zip(
                            repository.findEmployees(yearStart, yearEnd).collectList(),
                            repository.findProjects().collectList(),
                            repository.findAllocatedProjectIds(year).collect(java.util.stream.Collectors.toSet()),
                            repository.findClosedPeriods(year).collect(java.util.stream.Collectors.toSet()))
                    .flatMap(data -> {
                        var projects = data.getT2().stream()
                                .map(project -> toProjectDto(project, selectedYear, auth))
                                .filter(project -> project.active() || data.getT3().contains(project.id()))
                                .filter(ProjectDto::editable)
                                .sorted(Comparator.comparing(ProjectDto::name))
                                .toList();
                        var selectedProjectId = requestedProjectId == null
                                ? projects.stream().findFirst().map(ProjectDto::id).orElse(null)
                                : requestedProjectId;
                        if (selectedProjectId != null
                                && projects.stream().noneMatch(project -> project.id().equals(selectedProjectId))) {
                            return Mono.error(new BusinessError("errors.resource_allocation.invalid_reference"));
                        }
                        var months = IntStream.range(0, 12)
                                .map(index -> year * 100 + index)
                                .mapToObj(period -> new MonthDto(period, data.getT4().contains(period)))
                                .toList();
                        var employees = data.getT1().stream().map(this::toInputEmployeeDto).toList();
                        if (selectedProjectId == null) {
                            return Mono.just(new ResourceAllocationProjectInputDto(year, null, null, months,
                                    employees, projects, List.of(), List.of(), List.of()));
                        }
                        return workstreamRepo.findActiveByProjectId(selectedProjectId).collectList().flatMap(workstreams -> {
                            if (requestedWorkstreamId != null && workstreams.stream()
                                    .noneMatch(workstream -> workstream.getId().equals(requestedWorkstreamId))) {
                                return Mono.error(new BusinessError("errors.project.workstream.invalid"));
                            }
                            return Mono.zip(
                                        repository.findProjectAllocations(selectedProjectId, requestedWorkstreamId, year)
                                                .map(this::toProjectInputAllocationDto).collectList(),
                                        repository.findOtherProjectAllocations(selectedProjectId, requestedWorkstreamId, year)
                                                .map(allocation -> new ResourceAllocationProjectInputDto.OtherAllocationDto(
                                                        allocation.period(), allocation.employeeId(),
                                                        allocation.percent(), allocation.sameProject()))
                                                .collectList())
                                    .map(allocations -> new ResourceAllocationProjectInputDto(year, selectedProjectId,
                                            requestedWorkstreamId, months, employees, projects,
                                            workstreams.stream().map(this::toWorkstreamDto).toList(),
                                            allocations.getT1(), allocations.getT2()));
                        });
                    });
        }));
    }

    /**
     * Saves one project/year draft as a single revision.
     */
    @Transactional
    public Mono<Integer> save(int year, int projectId, Integer workstreamId,
                              ResourceAllocationSaveBody request, AuthContext auth) {
        log.info("Saving {} resource allocation changes for project {} and year {} by {}",
                request.changes() == null ? 0 : request.changes().size(), projectId, year, auth.getUsername());
        return securityValidator.validateCanWriteAllocations(auth).then(Mono.defer(() -> {
            var selectedYear = parseYear(year);
            validateChanges(year, request.changes());
            var periods = request.changes().stream().map(Change::period).distinct().sorted().toList();
            return Flux.fromIterable(periods).concatMap(repository::lockPeriod).then(Mono.zip(
                            repository.findEmployees(selectedYear.atDay(1),
                                    selectedYear.atMonth(12).atEndOfMonth()).collectList(),
                            repository.findProjects().collectList(),
                            repository.findProjectAllocations(projectId, workstreamId, year).collectList(),
                            repository.findClosedPeriods(year).collect(java.util.stream.Collectors.toSet())))
                    .flatMap(data -> validateWorkstream(projectId, workstreamId)
                            .then(saveValidated(year, projectId, workstreamId, request.changes(), auth,
                                    data.getT1(), data.getT2(), data.getT3(), data.getT4())));
        }));
    }

    private Mono<Integer> saveValidated(int year, int projectId, Integer workstreamId,
                                        List<Change> requested, AuthContext auth,
                                        List<ResourceAllocationEmployeeView> employees,
                                        List<ResourceAllocationProjectView> projects,
                                        List<PeriodResourceAllocationView> existing,
                                        java.util.Set<Integer> closedPeriods) {
        var employeeById = employees.stream().collect(java.util.stream.Collectors.toMap(
                ResourceAllocationEmployeeView::id, employee -> employee));
        var project = projects.stream().filter(value -> value.id().equals(projectId)).findFirst().orElse(null);
        if (project == null) {
            return Mono.error(new BusinessError("errors.resource_allocation.invalid_reference"));
        }
        if (!securityValidator.canWriteProject(auth, project)) {
            return Mono.error(new org.springframework.security.access.AccessDeniedException(
                    "No access to resource allocation project " + projectId));
        }
        for (var change : requested) {
            var employee = employeeById.get(change.employeeId());
            if (change.percent() != null
                    && (employee == null || !employmentOverlaps(employee, parsePeriod(change.period())))) {
                return Mono.error(new BusinessError("errors.resource_allocation.employee_not_employed",
                        change.employeeId().toString(), change.period().toString()));
            }
            if (closedPeriods.contains(change.period())) {
                return Mono.error(new BusinessError("errors.resource_allocation.period_closed",
                        change.period().toString()));
            }
        }

        var current = new HashMap<CellKey, PeriodResourceAllocationView>();
        existing.forEach(value -> current.put(new CellKey(value.period(), value.employeeId()), value));
        var conflicts = requested.stream().filter(change -> {
            var currentValue = current.get(new CellKey(change.period(), change.employeeId()));
            return !Objects.equals(currentValue == null ? null : currentValue.revisionId(),
                    change.expectedRevisionId());
        }).toList();
        if (!conflicts.isEmpty()) {
            var conflictKeys = conflicts.stream()
                    .map(change -> new CellKey(change.period(), change.employeeId()))
                    .collect(java.util.stream.Collectors.toSet());
            var rebasedChanges = requested.stream()
                    .filter(change -> !conflictKeys.contains(new CellKey(change.period(), change.employeeId())))
                    .filter(change -> !Objects.equals(currentPercent(current.get(new CellKey(change.period(), change.employeeId()))),
                            change.percent()))
                    .toList();
            return Mono.error(conflict(existing, rebasedChanges, conflicts));
        }
        var changes = requested.stream()
                .filter(change -> !Objects.equals(currentPercent(current.get(new CellKey(change.period(), change.employeeId()))),
                        change.percent()))
                .toList();
        if (changes.isEmpty()) {
            return Mono.error(new BusinessError("errors.resource_allocation.no_changes"));
        }

        return repository.createRevision(year, projectId, workstreamId, dateTimeService.now(),
                        auth.getEmployeeInfo().getEmployeeId())
                .flatMap(revisionId -> Flux.fromIterable(changes)
                        .concatMap(change -> persistChange(year, projectId, workstreamId, revisionId, change,
                                current.get(new CellKey(change.period(), change.employeeId()))))
                        .then(Mono.just(revisionId)));
    }

    private Mono<Long> persistChange(int year, int projectId, Integer workstreamId, int revisionId, Change change,
                                     PeriodResourceAllocationView previousValue) {
        Mono<Long> update;
        if (change.percent() == null) {
            update = repository.deleteIfRevisionMatches(year, change.period(), change.employeeId(), projectId, workstreamId,
                    change.expectedRevisionId());
        } else if (change.expectedRevisionId() == null) {
            update = repository.insertIfAbsent(year, change.period(), change.employeeId(), projectId, workstreamId,
                    change.percent(), revisionId);
        } else {
            update = repository.updateIfRevisionMatches(year, change.period(), change.employeeId(), projectId, workstreamId,
                    change.percent(), revisionId, change.expectedRevisionId());
        }
        return repository.recordChange(revisionId, change.period(), change.employeeId(),
                        currentPercent(previousValue), change.percent())
                .then(update)
                .flatMap(updated -> updated == 1 ? Mono.just(updated) : Mono.error(conflict(change)));
    }

    private BusinessError conflict(Change change) {
        var error = new BusinessError(HttpStatus.CONFLICT, "errors.resource_allocation.conflict");
        error.setAttrs(Map.of(
                "employeeId", change.employeeId().toString(),
                "period", change.period().toString()));
        return error;
    }

    private BusinessError conflict(List<PeriodResourceAllocationView> existing, List<Change> rebasedChanges,
                                   List<Change> conflicts) {
        var error = new BusinessError(HttpStatus.CONFLICT, "errors.resource_allocation.conflict");
        error.setAttrs(Map.of(
                "allocations", existing.stream().map(this::toProjectInputAllocationDto).toList(),
                "changes", rebasedChanges,
                "conflicts", conflicts.stream().map(change -> Map.of(
                        "employeeId", change.employeeId(),
                        "period", change.period())).toList()));
        return error;
    }

    /**
     * Replaces the closed-period selection for one year and records only actual state changes.
     */
    @Transactional
    public Mono<List<Integer>> saveClosedPeriods(int year, List<Integer> closedPeriods, AuthContext auth) {
        return securityValidator.validateAdmin(auth).then(Mono.defer(() -> {
            parseYear(year);
            var desired = validateClosedPeriods(year, closedPeriods);
            return Flux.range(0, 12)
                    .concatMap(month -> repository.lockPeriod(year * 100 + month))
                    .then(repository.findClosedPeriods(year).collect(java.util.stream.Collectors.toSet()))
                    .flatMap(current -> {
                        var toClose = desired.stream().filter(period -> !current.contains(period)).toList();
                        var toOpen = current.stream().filter(period -> !desired.contains(period)).sorted().toList();
                        if (toClose.isEmpty() && toOpen.isEmpty()) {
                            return Mono.just(desired);
                        }
                        var changedAt = dateTimeService.now();
                        var changedBy = auth.getEmployeeInfo().getEmployeeId();
                        log.info("Changing {} resource allocation period states for year {} by {}",
                                toClose.size() + toOpen.size(), year, auth.getUsername());
                        return Flux.fromIterable(toClose)
                                .concatMap(period -> repository.closePeriod(year, period, changedAt, changedBy))
                                .thenMany(Flux.fromIterable(toOpen)
                                        .concatMap(period -> repository.reopenPeriod(period, changedAt, changedBy)))
                                .then(Mono.just(desired));
                    });
        }));
    }

    /**
     * Returns closed allocation periods for a calendar year.
     */
    @Transactional(readOnly = true)
    public Flux<Integer> getClosedPeriods(int year, AuthContext auth) {
        parseYear(year);
        return securityValidator.validateCanReadAllocations(auth)
                .thenMany(repository.findClosedPeriods(year));
    }

    private EmployeeDto toEmployeeDto(ResourceAllocationEmployeeView employee) {
        return new EmployeeDto(employee.id(), employee.displayName(),
                employee.departmentId(), employee.departmentName(),
                employee.currentProjectId(), employee.currentProjectName(), employee.currentProjectRole(),
                employee.email());
    }

    private ResourceAllocationProjectInputDto.AllocationDto toProjectInputAllocationDto(
            PeriodResourceAllocationView allocation) {
        return new ResourceAllocationProjectInputDto.AllocationDto(allocation.period(), allocation.employeeId(),
                allocation.percent(), allocation.revisionId());
    }

    private ResourceAllocationProjectInputDto.EmployeeDto toInputEmployeeDto(
            ResourceAllocationEmployeeView employee) {
        var dismissalDate = employee.dateOfDismissal();
        return new ResourceAllocationProjectInputDto.EmployeeDto(employee.id(), employee.displayName(),
                employee.currentProjectId(), employee.currentProjectName(), employee.currentProjectRole(),
                employee.dateOfEmployment(),
                dismissalDate, dismissalDate != null && !dismissalDate.isAfter(dateTimeService.now().toLocalDate()),
                employee.email());
    }

    private ProjectDto toProjectDto(ResourceAllocationProjectView project, Year year, AuthContext auth) {
        var active = (project.startDate() == null || !project.startDate().isAfter(year.atMonth(12).atEndOfMonth()))
                && (project.endDate() == null || !project.endDate().isBefore(year.atDay(1)));
        return new ProjectDto(project.id(), project.name(), project.departmentId(), project.departmentName(),
                project.baId(), project.baName(), project.startDate(), project.endDate(), active,
                securityValidator.canWriteProject(auth, project));
    }

    private YearMonth parsePeriod(int period) {
        try {
            return YearMonth.of(period / 100, period % 100 + 1);
        } catch (DateTimeException error) {
            throw new BusinessError("errors.resource_allocation.invalid_period", Integer.toString(period));
        }
    }

    private Year parseYear(int year) {
        try {
            return Year.of(year);
        } catch (DateTimeException error) {
            throw new BusinessError("errors.resource_allocation.invalid_year", Integer.toString(year));
        }
    }

    private void validateChanges(int year, List<Change> changes) {
        if (changes == null || changes.isEmpty()) {
            throw new BusinessError("errors.resource_allocation.no_changes");
        }
        var cells = new HashSet<CellKey>();
        for (var change : changes) {
            if (change == null || change.period() == null || change.employeeId() == null
                    || change.period() / 100 != year
                    || (change.percent() != null && (change.percent() < 0 || change.percent() > 1000))
                    || (change.expectedRevisionId() != null && change.expectedRevisionId() <= 0)
                    || !cells.add(new CellKey(change.period(), change.employeeId()))) {
                throw new BusinessError("errors.resource_allocation.invalid_changes");
            }
            parsePeriod(change.period());
        }
    }

    private List<Integer> validateClosedPeriods(int year, List<Integer> periods) {
        if (periods == null) {
            throw new BusinessError("errors.resource_allocation.invalid_period", "null");
        }
        var result = new HashSet<Integer>();
        for (var period : periods) {
            if (period == null || period / 100 != year) {
                throw new BusinessError("errors.resource_allocation.invalid_period", String.valueOf(period));
            }
            parsePeriod(period);
            result.add(period);
        }
        return result.stream().sorted().toList();
    }

    private boolean employmentOverlaps(ResourceAllocationEmployeeView employee, YearMonth month) {
        return (employee.dateOfEmployment() == null || !employee.dateOfEmployment().isAfter(month.atEndOfMonth()))
                && (employee.dateOfDismissal() == null
                || !employee.dateOfDismissal().isBefore(month.atDay(1)));
    }

    private Integer currentPercent(PeriodResourceAllocationView allocation) {
        return allocation == null ? null : allocation.percent();
    }

    private Mono<Void> validateWorkstream(int projectId, Integer workstreamId) {
        if (workstreamId == null) {
            return Mono.empty();
        }
        return workstreamRepo.findById(workstreamId)
                .filter(workstream -> workstream.getDeletedAt() == null && workstream.getProjectId() == projectId)
                .switchIfEmpty(Mono.error(new BusinessError("errors.project.workstream.invalid")))
                .then();
    }

    private ProjectWorkstreamDto toWorkstreamDto(ProjectWorkstreamEntry workstream) {
        return new ProjectWorkstreamDto(workstream.getId(), workstream.getExternalId(),
                workstream.getDisplayName(), workstream.getDescription());
    }

    private record CellKey(int period, int employeeId) {
    }
}
