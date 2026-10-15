package ru.abondin.hreasy.platform.service.external;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.web.server.ResponseStatusException;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
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
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto;
import ru.abondin.hreasy.platform.service.dict.DictService;
import ru.abondin.hreasy.platform.service.dto.CurrentProjectDictDto;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.dto.ProjectDictDto;
import ru.abondin.hreasy.platform.service.external.dto.ExternalApiDto;
import ru.abondin.hreasy.platform.service.overtime.OvertimeService;
import ru.abondin.hreasy.platform.service.overtime.dto.ExternalOvertimeSummaryDto;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeEmployeeSummary;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.time.YearMonth;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class ExternalApiServiceTest {
    private final EmployeeService employees = mock(EmployeeService.class);
    private final EmployeeRepo employeeRepo = mock(EmployeeRepo.class);
    private final OvertimeService overtimes = mock(OvertimeService.class);
    private final ResourceAllocationService allocations = mock(ResourceAllocationService.class);
    private final DictService dictionaries = mock(DictService.class);
    private final BusinessAccountRepo baRepo = mock(BusinessAccountRepo.class);
    private final ProjectWorkstreamRepo workstreamRepo = mock(ProjectWorkstreamRepo.class);
    private final FileStorage files = mock(FileStorage.class);
    private final DateTimeService clock = mock(DateTimeService.class);
    private final AuthContext auth = mock(AuthContext.class);
    private final ExternalApiService service = new ExternalApiService(employees, employeeRepo, overtimes,
            allocations, dictionaries, baRepo, workstreamRepo, files, clock);
    private final LocalDate date = LocalDate.of(2026, 9, 15);

    @BeforeEach
    void catalog() {
        var project = new ProjectDictDto();
        project.setId(301);
        project.setName("Example project");
        project.setExternalId("project-alpha");
        project.setBaId(501);
        when(dictionaries.findProjects(auth)).thenReturn(Flux.just(project));
        var ba = new BusinessAccountEntry();
        ba.setId(501);
        ba.setName("Example account");
        ba.setExternalId("account-alpha");
        when(baRepo.findAll()).thenReturn(Flux.just(ba));
        var oldStream = stream(401, "Previous name", "delivery");
        oldStream.setDeletedAt(OffsetDateTime.parse("2026-09-01T00:00:00Z"));
        when(workstreamRepo.findAll()).thenReturn(Flux.just(oldStream, stream(402, "Delivery", "delivery")));
        when(clock.now()).thenReturn(OffsetDateTime.parse("2026-10-01T12:00:00Z"));
    }

    @Test
    void exportsOnlyMinimalActiveProfilesAndPreservesEmailExactly() {
        var employee = new EmployeeDto();
        employee.setId(201);
        employee.setEmail("Alex.Morgan@example.test");
        employee.setDisplayName("Alex Morgan");
        employee.setCurrentProject(new CurrentProjectDictDto(301, "Example project", "Developer"));
        employee.setTelegram("private-profile-field");
        when(employees.findAll(auth, false)).thenReturn(Flux.just(employee));
        StepVerifier.create(service.employees(auth)).assertNext(result -> {
            assertEquals(employee.getEmail(), result.email());
            assertEquals("account-alpha", result.currentProject().ba().externalId());
            var json = new ObjectMapper().valueToTree(result);
            assertEquals(6, json.size());
            for (var forbidden : List.of("id", "skills", "ratings", "officeLocation", "officeWorkplace", "telegram", "birthday", "sex")) {
                assertFalse(json.has(forbidden), forbidden);
            }
            assertFalse(json.get("currentProject").has("id"));
        }).verifyComplete();
        verify(employees).findAll(auth, false);
    }

    @Test
    void retainsHistoricalOvertimeByEmailAndMergesReusedWorkstreamKeys() {
        var employee = new EmployeeEntry();
        employee.setId(201);
        employee.setEmail("former.employee@example.test");
        employee.setDateOfDismissal(date);
        when(employeeRepo.findAll()).thenReturn(Flux.just(employee));
        when(overtimes.getExternalSummary(202608, auth)).thenReturn(Flux.just(new ExternalOvertimeSummaryDto(
                201, 601, 6, null, null, null, OvertimeEmployeeSummary.OvertimeApprovalCommonStatus.NO_DECISIONS,
                List.of(new ExternalOvertimeSummaryDto.ItemDto(date, 301, 601, 401, "delivery", "Previous name", 2),
                        new ExternalOvertimeSummaryDto.ItemDto(date, 301, 601, 402, "delivery", "Delivery", 3),
                        new ExternalOvertimeSummaryDto.ItemDto(date, 301, 601, null, null, null, 1)))));
        StepVerifier.create(service.overtimes(YearMonth.of(2026, 9), auth)).assertNext(result -> {
            assertEquals("former.employee@example.test", result.employeeEmail());
            assertEquals("2026-09", result.period());
            assertEquals(2, result.items().size());
            assertEquals(5, result.items().getFirst().hours());
            assertEquals("Delivery", result.items().getFirst().workstream().name());
            assertEquals("project-alpha", result.items().getFirst().project().externalId());
            assertNull(result.items().getLast().workstream());
        }).verifyComplete();
    }

    @Test
    void allocationsContainOnlyEmailsAndCellsIncludingExplicitZeros() {
        var historicalEmployee = new ResourceAllocationAnalyticsDto.EmployeeDto(201, "Former Employee", null,
                null, null, null, null, "former.employee@example.test");
        when(allocations.getAnalytics(2026, auth)).thenReturn(Mono.just(new ResourceAllocationAnalyticsDto(2026,
                List.of(historicalEmployee), List.of(), List.of(), List.of(
                new ResourceAllocationAnalyticsDto.AllocationDto(202608, 201, 301, 401, 20),
                new ResourceAllocationAnalyticsDto.AllocationDto(202608, 201, 301, 402, 30),
                new ResourceAllocationAnalyticsDto.AllocationDto(202608, 201, 301, null, 0)))));
        StepVerifier.create(service.allocations(2026, auth)).assertNext(result -> {
            assertEquals(2, result.allocations().size());
            var cell = result.allocations().getFirst();
            assertEquals("former.employee@example.test", cell.employeeEmail());
            assertEquals("2026-09", cell.period());
            assertEquals(50, cell.percent());
            assertEquals(new ExternalApiDto.Reference("Example account", "account-alpha"), cell.project().ba());
            assertNull(result.allocations().getLast().workstream());
            assertEquals(0, result.allocations().getLast().percent());
            assertFalse(new ObjectMapper().valueToTree(result).has("employees"));
        }).verifyComplete();
    }

    @Test
    void doesNotMergeDifferentWorkstreamsWithoutExternalKeys() {
        when(workstreamRepo.findAll()).thenReturn(Flux.just(stream(401, "First", null), stream(402, "Second", null)));
        when(allocations.getAnalytics(2026, auth)).thenReturn(Mono.just(new ResourceAllocationAnalyticsDto(2026,
                List.of(new ResourceAllocationAnalyticsDto.EmployeeDto(201, "Alex Morgan", null, null, null, null, null, "alex.morgan@example.test")),
                List.of(), List.of(), List.of(
                new ResourceAllocationAnalyticsDto.AllocationDto(202608, 201, 301, 401, 20),
                new ResourceAllocationAnalyticsDto.AllocationDto(202608, 201, 301, 402, 30)))));
        StepVerifier.create(service.allocations(2026, auth))
                .assertNext(result -> assertEquals(2, result.allocations().size())).verifyComplete();
    }

    @Test
    void refusesDismissedEmployeeAvatarButAllowsActiveEmployee() {
        var employee = new EmployeeEntry();
        employee.setId(201);
        employee.setDateOfDismissal(LocalDate.of(2026, 10, 1));
        when(employeeRepo.findIdByEmailIgnoreCase("alex.morgan@example.test")).thenReturn(Mono.just(201));
        when(employeeRepo.findById(201)).thenReturn(Mono.just(employee));
        StepVerifier.create(service.avatar("alex.morgan@example.test", auth))
                .expectErrorMatches(error -> error instanceof ResponseStatusException status && status.getStatusCode().value() == 404)
                .verify();
        verify(employees, never()).avatar(anyInt(), any());
        employee.setDateOfDismissal(null);
        when(employees.avatar(201, auth)).thenReturn(Mono.just(new ByteArrayResource(new byte[]{1})));
        StepVerifier.create(service.avatar("alex.morgan@example.test", auth)).expectNextCount(1).verifyComplete();
    }

    private ProjectWorkstreamEntry stream(int id, String name, String externalId) {
        var stream = new ProjectWorkstreamEntry();
        stream.setId(id);
        stream.setProjectId(301);
        stream.setDisplayName(name);
        stream.setExternalId(externalId);
        return stream;
    }
}
