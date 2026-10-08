package ru.abondin.hreasy.platform.service.external;

import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountEntry;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountRepo;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamEntry;
import ru.abondin.hreasy.platform.repo.dict.ProjectWorkstreamRepo;
import ru.abondin.hreasy.platform.repo.employee.EmployeeEntry;
import ru.abondin.hreasy.platform.repo.employee.EmployeeRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.EmployeeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.allocation.ResourceAllocationService;
import ru.abondin.hreasy.platform.service.dict.DictService;
import ru.abondin.hreasy.platform.service.dto.ProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.SimpleDictDto;
import ru.abondin.hreasy.platform.service.external.dto.ExternalApiDto.*;
import ru.abondin.hreasy.platform.service.overtime.OvertimeService;

import java.time.LocalDate;
import java.time.YearMonth;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/** Adapts authorized domain reads to the minimal external contract. */
@Service
@RequiredArgsConstructor
public class ExternalApiService {
    private final EmployeeService employeeService;
    private final EmployeeRepo employeeRepo;
    private final OvertimeService overtimeService;
    private final ResourceAllocationService allocationService;
    private final DictService dictService;
    private final BusinessAccountRepo baRepo;
    private final ProjectWorkstreamRepo workstreamRepo;
    private final FileStorage fileStorage;
    private final DateTimeService dateTimeService;

    /** Only active profiles are exported; web-only profile attributes never enter the response. */
    public Flux<Employee> employees(AuthContext auth) {
        return catalog(auth).flatMapMany(catalog -> employeeService.findAll(auth, false)
                .map(employee -> new Employee(employee.getEmail(), employee.getDisplayName(),
                        name(employee.getDepartment()), name(employee.getPosition()),
                        employee.getCurrentProject() == null ? null : catalog.project(employee.getCurrentProject().getId()),
                        fileStorage.fileExists("avatars", employee.getId() + ".png"))));
    }

    /** Returns an active employee's avatar; dismissed employees are indistinguishable from missing employees. */
    public Mono<Resource> avatar(String email, AuthContext auth) {
        if (email == null || email.isBlank()) {
            return Mono.error(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Employee email must not be blank"));
        }
        return employeeRepo.findIdByEmailIgnoreCase(email.trim())
                .flatMap(employeeRepo::findById)
                .filter(employee -> employee.getDateOfDismissal() == null
                        || employee.getDateOfDismissal().isAfter(dateTimeService.now().toLocalDate()))
                .switchIfEmpty(Mono.error(new ResponseStatusException(HttpStatus.NOT_FOUND, "Employee not found")))
                .flatMap(employee -> employeeService.avatar(employee.getId(), auth));
    }

    /** Projects retain both display names and configured integration keys. */
    public Flux<Project> projects(AuthContext auth) {
        return catalog(auth).flatMapMany(catalog -> Flux.fromIterable(catalog.projects().values())
                .map(project -> new Project(project.getName(), project.getExternalId(), catalog.ba(project.getBaId()),
                        project.isActive(), project.getWorkstreams().stream()
                        .map(workstream -> new Reference(workstream.displayName(), workstream.externalId())).toList())));
    }

    /** Retains historical hours using email, without exporting dismissed employee profiles. */
    public Flux<Overtime> overtimes(YearMonth period, AuthContext auth) {
        var internalPeriod = period.getYear() * 100 + period.getMonthValue() - 1;
        return Mono.zip(catalog(auth), employeeRepo.findAll().collectMap(EmployeeEntry::getId, EmployeeEntry::getEmail))
                .flatMapMany(data -> overtimeService.getExternalSummary(internalPeriod, auth).map(report -> {
                    var catalog = data.getT1();
                    var items = new LinkedHashMap<CellKey, OvertimeItem>();
                    for (var item : report.items()) {
                        var key = catalog.key(item.date(), item.projectId(), item.workstreamId());
                        items.merge(key, new OvertimeItem(item.date(), catalog.project(item.projectId()),
                                        catalog.workstream(item.workstreamId()), item.hours()),
                                (left, right) -> new OvertimeItem(left.date(), left.project(), left.workstream(),
                                        left.hours() + right.hours()));
                    }
                    return new Overtime(data.getT2().get(report.employeeId()), period.toString(), report.totalHours(),
                            report.lastUpdate(), report.lastApprove(), report.lastDecline(),
                            report.commonApprovalStatus().name(), List.copyOf(items.values()));
                }));
    }

    /** Keeps existing allocation visibility while replacing internal references with business keys. */
    public Mono<Allocations> allocations(int year, AuthContext auth) {
        return allocationService.getAnalytics(year, auth).flatMap(analytics -> catalog(auth).map(catalog -> {
            var emails = new HashMap<Integer, String>();
            analytics.employees().forEach(employee -> emails.put(employee.id(), employee.email()));
            var allocations = new LinkedHashMap<AllocationKey, Allocation>();
            for (var cell : analytics.allocations()) {
                var period = YearMonth.of(cell.period() / 100, cell.period() % 100 + 1);
                var key = new AllocationKey(cell.employeeId(), catalog.key(period.atDay(1), cell.projectId(), cell.workstreamId()));
                allocations.merge(key, new Allocation(emails.get(cell.employeeId()), period.toString(),
                                catalog.project(cell.projectId()), catalog.workstream(cell.workstreamId()), cell.percent()),
                        (left, right) -> new Allocation(left.employeeEmail(), left.period(), left.project(), left.workstream(),
                                left.percent() + right.percent()));
            }
            return new Allocations(year, List.copyOf(allocations.values()));
        }));
    }

    private Mono<Catalog> catalog(AuthContext auth) {
        return Mono.zip(dictService.findProjects(auth).collectMap(ProjectDictDto::getId, project -> project, LinkedHashMap::new),
                        baRepo.findAll().collectMap(BusinessAccountEntry::getId), workstreamRepo.findAll().collectList())
                .map(data -> {
                    var canonical = new HashMap<WorkstreamKey, Reference>();
                    data.getT3().stream()
                            .sorted(Comparator.comparing(ProjectWorkstreamEntry::getDeletedAt,
                                    Comparator.nullsLast(Comparator.naturalOrder())))
                            .filter(workstream -> workstream.getExternalId() != null)
                            .forEach(workstream -> canonical.put(new WorkstreamKey(workstream.getProjectId(), workstream.getExternalId()),
                                    new Reference(workstream.getDisplayName(), workstream.getExternalId())));
                    var workstreams = new HashMap<Integer, Reference>();
                    data.getT3().forEach(workstream -> workstreams.put(workstream.getId(),
                            canonical.getOrDefault(new WorkstreamKey(workstream.getProjectId(), workstream.getExternalId()),
                                    new Reference(workstream.getDisplayName(), workstream.getExternalId()))));
                    return new Catalog(data.getT1(), data.getT2(), workstreams);
                });
    }

    private static String name(SimpleDictDto value) {
        return value == null ? null : value.getName();
    }

    private record WorkstreamKey(Integer projectId, String externalId) {
    }

    private record CellKey(LocalDate date, Integer projectId, String workstreamExternalId, Integer unconfiguredWorkstreamId) {
    }

    private record AllocationKey(Integer employeeId, CellKey cell) {
    }

    private record Catalog(Map<Integer, ProjectDictDto> projects, Map<Integer, BusinessAccountEntry> businessAccounts,
                           Map<Integer, Reference> workstreams) {
        Reference ba(Integer id) {
            var ba = businessAccounts.get(id);
            return ba == null ? null : new Reference(ba.getName(), ba.getExternalId());
        }

        ProjectReference project(Integer id) {
            var project = projects.get(id);
            return new ProjectReference(project.getName(), project.getExternalId(), ba(project.getBaId()));
        }

        Reference workstream(Integer id) {
            return workstreams.get(id);
        }

        CellKey key(LocalDate date, Integer projectId, Integer workstreamId) {
            var workstream = workstream(workstreamId);
            var externalId = workstream == null ? null : workstream.externalId();
            return new CellKey(date, projectId, externalId, externalId == null ? workstreamId : null);
        }
    }
}
