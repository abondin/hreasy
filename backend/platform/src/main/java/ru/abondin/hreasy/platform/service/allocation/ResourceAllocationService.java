package ru.abondin.hreasy.platform.service.allocation;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationEmployeeView;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationProjectView;
import ru.abondin.hreasy.platform.repo.allocation.ResourceAllocationRepository.ResourceAllocationView;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSaveBody;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSaveBody.Change;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto.AllocationDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto.EmployeeDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto.ProjectDto;

import java.time.DateTimeException;
import java.time.YearMonth;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

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

    /**
     * Loads employees, projects, current allocations, and project editability for one period.
     * The repository-wide period convention uses a zero-based month, so {@code 202600} is January 2026.
     */
    @Transactional(readOnly = true)
    public Mono<ResourceAllocationSheetDto> getSheet(int period, AuthContext auth) {
        return securityValidator.validateCanEditAllocations(auth).then(Mono.defer(() -> {
            var month = parsePeriod(period);
            return Mono.zip(
                            repository.findEmployees(month.atDay(1), month.atEndOfMonth()).collectList(),
                            repository.findProjects().collectList(),
                            repository.findAllocations(period).collectList())
                    .map(data -> new ResourceAllocationSheetDto(period,
                            data.getT1().stream().map(this::toEmployeeDto).toList(),
                            data.getT2().stream().map(project -> toProjectDto(project, month, auth)).toList(),
                            data.getT3().stream().map(this::toAllocationDto).toList()));
        }));
    }

    /**
     * Validates and saves only changed cells. Every successful call creates exactly one revision.
     */
    @Transactional
    public Mono<Integer> save(int period, ResourceAllocationSaveBody request, AuthContext auth) {
        log.info("Saving {} resource allocation changes for period {} by {}",
                request.changes() == null ? 0 : request.changes().size(), period, auth.getUsername());
        return securityValidator.validateCanEditAllocations(auth).then(Mono.defer(() -> {
            var month = parsePeriod(period);
            validateChanges(request.changes());
            return Mono.zip(
                            repository.findEmployees(month.atDay(1), month.atEndOfMonth()).collectList(),
                            repository.findProjects().collectList(),
                            repository.findAllocations(period).collectList())
                    .flatMap(data -> saveValidated(period, request.changes(), auth,
                            data.getT1(), data.getT2(), data.getT3()));
        }));
    }

    private Mono<Integer> saveValidated(int period, List<Change> requested, AuthContext auth,
                                        List<ResourceAllocationEmployeeView> employees,
                                        List<ResourceAllocationProjectView> projects,
                                        List<ResourceAllocationView> existing) {
        var employeeIds = employees.stream().map(ResourceAllocationEmployeeView::id)
                .collect(java.util.stream.Collectors.toSet());
        var projectById = projects.stream().collect(java.util.stream.Collectors.toMap(
                ResourceAllocationProjectView::id, project -> project));
        for (var change : requested) {
            if (!employeeIds.contains(change.employeeId()) || !projectById.containsKey(change.projectId())) {
                return Mono.error(new BusinessError("errors.resource_allocation.invalid_reference"));
            }
            securityValidator.validateEditProject(auth, projectById.get(change.projectId()));
        }

        var current = new HashMap<CellKey, Integer>();
        existing.forEach(value -> current.put(new CellKey(value.employeeId(), value.projectId()), value.percent()));
        var changes = requested.stream()
                .filter(change -> current.getOrDefault(new CellKey(change.employeeId(), change.projectId()), 0) != change.percent())
                .toList();
        if (changes.isEmpty()) {
            return Mono.error(new BusinessError("errors.resource_allocation.no_changes"));
        }

        return repository.createRevision(period, dateTimeService.now(), auth.getEmployeeInfo().getEmployeeId())
                .flatMap(revisionId -> Flux.fromIterable(changes)
                        .concatMap(change -> persistChange(period, revisionId, change,
                                current.getOrDefault(new CellKey(change.employeeId(), change.projectId()), 0)))
                        .then(Mono.just(revisionId)));
    }

    private Mono<Long> persistChange(int period, int revisionId, Change change, int previousPercent) {
        var update = change.percent() == 0
                ? repository.delete(period, change.employeeId(), change.projectId())
                : repository.upsert(period, change.employeeId(), change.projectId(), change.percent(), revisionId);
        return repository.recordChange(revisionId, change.employeeId(), change.projectId(), previousPercent, change.percent())
                .then(update);
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

    private ProjectDto toProjectDto(ResourceAllocationProjectView project, YearMonth month, AuthContext auth) {
        var active = (project.startDate() == null || !project.startDate().isAfter(month.atEndOfMonth()))
                && (project.endDate() == null || !project.endDate().isBefore(month.atDay(1)));
        return new ProjectDto(project.id(), project.name(), project.departmentId(), project.departmentName(),
                project.baId(), project.baName(), active, securityValidator.canEditProject(auth, project));
    }

    private YearMonth parsePeriod(int period) {
        try {
            return YearMonth.of(period / 100, period % 100 + 1);
        } catch (DateTimeException error) {
            throw new BusinessError("errors.resource_allocation.invalid_period", Integer.toString(period));
        }
    }

    private void validateChanges(List<Change> changes) {
        if (changes == null || changes.isEmpty()) {
            throw new BusinessError("errors.resource_allocation.no_changes");
        }
        var cells = new HashSet<CellKey>();
        for (var change : changes) {
            if (change == null || change.employeeId() == null || change.projectId() == null
                    || change.percent() < 0 || change.percent() > 1000
                    || !cells.add(new CellKey(change.employeeId(), change.projectId()))) {
                throw new BusinessError("errors.resource_allocation.invalid_changes");
            }
        }
    }

    private record CellKey(int employeeId, int projectId) {
    }
}
