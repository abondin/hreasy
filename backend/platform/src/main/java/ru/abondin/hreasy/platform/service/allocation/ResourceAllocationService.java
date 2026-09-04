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
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationView;
import ru.abondin.hreasy.platform.repo.manager.ManagerRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSaveBody;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSaveBody.Change;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationProjectInputDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationProjectInputDto.MonthDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto.AllocationDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto.EmployeeDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto.ProjectDto;

import java.time.DateTimeException;
import java.time.Year;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.stream.IntStream;

/**
 * Builds monthly allocation sheets and persists changed cells in immutable batch revisions.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ResourceAllocationService {
    private final ResourceAllocationRepository repository;
    private final ResourceAllocationSecurityValidator securityValidator;
    private final DateTimeService dateTimeService;
    private final ManagerRepo managerRepo;

    /**
     * Loads employees, projects, current and previous allocations, and project editability for one period.
     * The repository-wide period convention uses a zero-based month, so {@code 202600} is January 2026.
     */
    @Transactional(readOnly = true)
    public Mono<ResourceAllocationSheetDto> getSheet(int period, AuthContext auth) {
        return securityValidator.validateCanEditAllocations(auth).then(Mono.defer(() -> {
            var month = parsePeriod(period);
            return Mono.zip(
                            repository.findEmployees(month.atDay(1), month.atEndOfMonth()).collectList(),
                            repository.findProjects().collectList(),
                            repository.findAllocations(month.getYear(), period).collectList(),
                            managerRepo.findManagedProjectHierarchyIds(auth.getEmployeeInfo().getEmployeeId())
                                    .collect(java.util.stream.Collectors.toSet()),
                            repository.findAllocations(month.minusMonths(1).getYear(),
                                    toPeriod(month.minusMonths(1))).collectList())
                    .map(data -> new ResourceAllocationSheetDto(period,
                            data.getT1().stream().map(this::toEmployeeDto).toList(),
                            data.getT2().stream().map(project -> toProjectDto(project, month, auth,
                                    data.getT4().contains(project.id()))).toList(),
                            data.getT3().stream().map(this::toAllocationDto).toList(),
                            data.getT5().stream().map(this::toAllocationDto).toList()));
        }));
    }

    /**
     * Loads the non-empty allocation hierarchy for one calendar year.
     */
    @Transactional(readOnly = true)
    public Mono<ResourceAllocationAnalyticsDto> getAnalytics(int year, AuthContext auth) {
        return securityValidator.validateCanEditAllocations(auth).then(Mono.defer(() -> {
            var selectedYear = parseYear(year);
            return Mono.zip(
                            repository.findEmployees(selectedYear.atDay(1),
                                    selectedYear.atMonth(12).atEndOfMonth()).collectList(),
                            repository.findProjects().collectList(),
                            repository.findYearAllocations(year).collectList(),
                            managerRepo.findManagedProjectHierarchyIds(auth.getEmployeeInfo().getEmployeeId())
                                    .collect(java.util.stream.Collectors.toSet()))
                    .map(data -> {
                        var allocations = data.getT3();
                        var employeeIds = allocations.stream()
                                .map(PeriodResourceAllocationView::employeeId).collect(java.util.stream.Collectors.toSet());
                        var projectIds = allocations.stream()
                                .map(PeriodResourceAllocationView::projectId).collect(java.util.stream.Collectors.toSet());
                        return new ResourceAllocationAnalyticsDto(year,
                                data.getT1().stream().filter(employee -> employeeIds.contains(employee.id()))
                                        .map(this::toEmployeeDto).toList(),
                                data.getT2().stream().filter(project -> projectIds.contains(project.id()))
                                        .map(project -> toProjectDto(project, selectedYear, auth,
                                                data.getT4().contains(project.id())))
                                        .sorted(Comparator.comparing(ProjectDto::name)).toList(),
                                allocations.stream().map(allocation ->
                                        new ResourceAllocationAnalyticsDto.AllocationDto(allocation.period(),
                                                allocation.employeeId(), allocation.projectId(), allocation.percent()))
                                        .toList());
                    });
        }));
    }

    /**
     * Loads all twelve months of one calendar year for a single managed project.
     */
    @Transactional(readOnly = true)
    public Mono<ResourceAllocationProjectInputDto> getProjectInput(int year, Integer requestedProjectId,
                                                                   AuthContext auth) {
        return securityValidator.validateCanEditAllocations(auth).then(Mono.defer(() -> {
            var selectedYear = parseYear(year);
            var yearStart = selectedYear.atDay(1);
            var yearEnd = selectedYear.atMonth(12).atEndOfMonth();
            return Mono.zip(
                            repository.findEmployees(yearStart, yearEnd).collectList(),
                            repository.findProjects().collectList(),
                            managerRepo.findManagedProjectHierarchyIds(auth.getEmployeeInfo().getEmployeeId())
                                    .collect(java.util.stream.Collectors.toSet()),
                            repository.findAllocatedProjectIds(year).collect(java.util.stream.Collectors.toSet()),
                            repository.findClosedPeriods(year).collect(java.util.stream.Collectors.toSet()))
                    .flatMap(data -> {
                        var projects = data.getT2().stream()
                                .filter(project -> securityValidator.canEditGlobally(auth)
                                        || data.getT3().contains(project.id()))
                                .map(project -> toProjectDto(project, selectedYear, auth,
                                        data.getT3().contains(project.id())))
                                .filter(project -> project.active() || data.getT4().contains(project.id()))
                                .sorted(Comparator.comparing(ProjectDto::name))
                                .toList();
                        var selectedProjectId = requestedProjectId == null
                                ? projects.stream().filter(ProjectDto::managed).findFirst()
                                .or(() -> projects.stream().findFirst())
                                .map(ProjectDto::id).orElse(null)
                                : requestedProjectId;
                        if (selectedProjectId != null
                                && projects.stream().noneMatch(project -> project.id().equals(selectedProjectId))) {
                            return Mono.error(new BusinessError("errors.resource_allocation.invalid_reference"));
                        }
                        var months = IntStream.range(0, 12)
                                .map(index -> year * 100 + index)
                                .mapToObj(period -> new MonthDto(period, data.getT5().contains(period)))
                                .toList();
                        var employees = data.getT1().stream().map(this::toInputEmployeeDto).toList();
                        if (selectedProjectId == null) {
                            return Mono.just(new ResourceAllocationProjectInputDto(year, null, months,
                                    employees, projects, List.of(), List.of(),
                                    securityValidator.canManagePeriods(auth)));
                        }
                        return Mono.zip(
                                        repository.findProjectAllocations(selectedProjectId, year)
                                                .map(this::toProjectInputAllocationDto).collectList(),
                                        repository.findOtherProjectAllocations(selectedProjectId, year)
                                                .map(allocation -> new ResourceAllocationProjectInputDto.OtherAllocationDto(
                                                        allocation.period(), allocation.employeeId(),
                                                        allocation.percent()))
                                                .collectList())
                                .map(allocations -> new ResourceAllocationProjectInputDto(year, selectedProjectId,
                                        months, employees, projects, allocations.getT1(), allocations.getT2(),
                                        securityValidator.canManagePeriods(auth)));
                    });
        }));
    }

    /**
     * Saves one project/year draft as a single revision.
     */
    @Transactional
    public Mono<Integer> save(int year, int projectId, ResourceAllocationSaveBody request, AuthContext auth) {
        log.info("Saving {} resource allocation changes for project {} and year {} by {}",
                request.changes() == null ? 0 : request.changes().size(), projectId, year, auth.getUsername());
        return securityValidator.validateCanEditAllocations(auth).then(Mono.defer(() -> {
            var selectedYear = parseYear(year);
            validateChanges(year, request.changes());
            var periods = request.changes().stream().map(Change::period).distinct().sorted().toList();
            return Flux.fromIterable(periods).concatMap(repository::lockPeriod).then(Mono.zip(
                            repository.findEmployees(selectedYear.atDay(1),
                                    selectedYear.atMonth(12).atEndOfMonth()).collectList(),
                            repository.findProjects().collectList(),
                            repository.findProjectAllocations(projectId, year).collectList(),
                            managerRepo.findManagedProjectHierarchyIds(auth.getEmployeeInfo().getEmployeeId())
                                    .collect(java.util.stream.Collectors.toSet()),
                            repository.findClosedPeriods(year).collect(java.util.stream.Collectors.toSet())))
                    .flatMap(data -> saveValidated(year, projectId, request.changes(), auth,
                            data.getT1(), data.getT2(), data.getT3(), data.getT4(), data.getT5()));
        }));
    }

    private Mono<Integer> saveValidated(int year, int projectId, List<Change> requested, AuthContext auth,
                                        List<ResourceAllocationEmployeeView> employees,
                                        List<ResourceAllocationProjectView> projects,
                                        List<PeriodResourceAllocationView> existing,
                                        Set<Integer> managedProjectIds, Set<Integer> closedPeriods) {
        var employeeById = employees.stream().collect(java.util.stream.Collectors.toMap(
                ResourceAllocationEmployeeView::id, employee -> employee));
        var project = projects.stream().filter(value -> value.id().equals(projectId)).findFirst().orElse(null);
        if (project == null || (!securityValidator.canEditGlobally(auth) && !managedProjectIds.contains(projectId))) {
            return Mono.error(new BusinessError("errors.resource_allocation.invalid_reference"));
        }
        securityValidator.validateEditProject(auth, project);
        for (var change : requested) {
            var employee = employeeById.get(change.employeeId());
            if (employee == null || !employmentOverlaps(employee, parsePeriod(change.period()))) {
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
                    .filter(change -> currentPercent(current.get(new CellKey(change.period(), change.employeeId())))
                            != change.percent())
                    .toList();
            return Mono.error(conflict(existing, rebasedChanges, conflicts));
        }
        var changes = requested.stream()
                .filter(change -> currentPercent(current.get(new CellKey(change.period(), change.employeeId())))
                        != change.percent())
                .toList();
        if (changes.isEmpty()) {
            return Mono.error(new BusinessError("errors.resource_allocation.no_changes"));
        }

        return repository.createRevision(year, projectId, dateTimeService.now(),
                        auth.getEmployeeInfo().getEmployeeId())
                .flatMap(revisionId -> Flux.fromIterable(changes)
                        .concatMap(change -> persistChange(year, projectId, revisionId, change,
                                current.get(new CellKey(change.period(), change.employeeId()))))
                        .then(Mono.just(revisionId)));
    }

    private Mono<Long> persistChange(int year, int projectId, int revisionId, Change change,
                                     PeriodResourceAllocationView previousValue) {
        Mono<Long> update;
        if (change.percent() == 0) {
            update = repository.deleteIfRevisionMatches(year, change.period(), change.employeeId(), projectId,
                    change.expectedRevisionId());
        } else if (change.expectedRevisionId() == null) {
            update = repository.insertIfAbsent(year, change.period(), change.employeeId(), projectId,
                    change.percent(), revisionId);
        } else {
            update = repository.updateIfRevisionMatches(year, change.period(), change.employeeId(), projectId,
                    change.percent(), revisionId, change.expectedRevisionId());
        }
        return repository.recordChange(revisionId, change.period(), change.employeeId(),
                        previousValue == null ? 0 : previousValue.percent(), change.percent())
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
     * Closes one monthly allocation period for every project.
     */
    @Transactional
    public Mono<Integer> closePeriod(int period, String comment, AuthContext auth) {
        var month = parsePeriod(period);
        return securityValidator.validateManagePeriods(auth)
                .then(repository.lockPeriod(period))
                .then(repository.closePeriod(month.getYear(), period, dateTimeService.now(),
                        auth.getEmployeeInfo().getEmployeeId(), comment))
                .thenReturn(period);
    }

    /**
     * Reopens one monthly allocation period.
     */
    @Transactional
    public Mono<Void> reopenPeriod(int period, AuthContext auth) {
        parsePeriod(period);
        return securityValidator.validateManagePeriods(auth)
                .then(repository.lockPeriod(period))
                .then(repository.reopenPeriod(period))
                .then();
    }

    private EmployeeDto toEmployeeDto(ResourceAllocationEmployeeView employee) {
        return new EmployeeDto(employee.id(), employee.displayName(),
                employee.departmentId(), employee.departmentName(),
                employee.currentProjectId(), employee.currentProjectName());
    }

    private AllocationDto toAllocationDto(ResourceAllocationView allocation) {
        return new AllocationDto(allocation.employeeId(), allocation.projectId(),
                allocation.percent(), allocation.revisionId());
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
                employee.currentProjectId(), employee.currentProjectName(), employee.dateOfEmployment(),
                dismissalDate, dismissalDate != null && !dismissalDate.isAfter(dateTimeService.now().toLocalDate()));
    }

    private ProjectDto toProjectDto(ResourceAllocationProjectView project, YearMonth month, AuthContext auth,
                                    boolean managed) {
        var active = (project.startDate() == null || !project.startDate().isAfter(month.atEndOfMonth()))
                && (project.endDate() == null || !project.endDate().isBefore(month.atDay(1)));
        return new ProjectDto(project.id(), project.name(), project.departmentId(), project.departmentName(),
                project.baId(), project.baName(), active, securityValidator.canEditProject(auth, project), managed);
    }

    private ProjectDto toProjectDto(ResourceAllocationProjectView project, Year year, AuthContext auth,
                                    boolean managed) {
        var active = (project.startDate() == null || !project.startDate().isAfter(year.atMonth(12).atEndOfMonth()))
                && (project.endDate() == null || !project.endDate().isBefore(year.atDay(1)));
        return new ProjectDto(project.id(), project.name(), project.departmentId(), project.departmentName(),
                project.baId(), project.baName(), active, securityValidator.canEditProject(auth, project), managed);
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

    private int toPeriod(YearMonth month) {
        return month.getYear() * 100 + month.getMonthValue() - 1;
    }

    private void validateChanges(int year, List<Change> changes) {
        if (changes == null || changes.isEmpty()) {
            throw new BusinessError("errors.resource_allocation.no_changes");
        }
        var cells = new HashSet<CellKey>();
        for (var change : changes) {
            if (change == null || change.period() == null || change.employeeId() == null
                    || change.period() / 100 != year
                    || change.percent() < 0 || change.percent() > 1000
                    || (change.expectedRevisionId() != null && change.expectedRevisionId() <= 0)
                    || !cells.add(new CellKey(change.period(), change.employeeId()))) {
                throw new BusinessError("errors.resource_allocation.invalid_changes");
            }
            parsePeriod(change.period());
        }
    }

    private boolean employmentOverlaps(ResourceAllocationEmployeeView employee, YearMonth month) {
        return (employee.dateOfEmployment() == null || !employee.dateOfEmployment().isAfter(month.atEndOfMonth()))
                && (employee.dateOfDismissal() == null
                || !employee.dateOfDismissal().isBefore(month.atDay(1)));
    }

    private int currentPercent(PeriodResourceAllocationView allocation) {
        return allocation == null ? 0 : allocation.percent();
    }

    private record CellKey(int period, int employeeId) {
    }
}
