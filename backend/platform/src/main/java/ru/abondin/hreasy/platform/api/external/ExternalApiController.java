package ru.abondin.hreasy.platform.api.external;

import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.EmployeeService;
import ru.abondin.hreasy.platform.service.FileStorage;
import ru.abondin.hreasy.platform.service.allocation.ResourceAllocationService;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto;
import ru.abondin.hreasy.platform.service.dict.DictService;
import ru.abondin.hreasy.platform.service.dto.EmployeeDto;
import ru.abondin.hreasy.platform.service.dto.ProjectDictDto;
import ru.abondin.hreasy.platform.service.overtime.OvertimeService;
import ru.abondin.hreasy.platform.service.overtime.dto.OvertimeEmployeeSummary;

import java.time.YearMonth;

/**
 * Read-only integration API that applies the acting HR Easy user's current access rules.
 */
@RestController
@RequestMapping("/external/api/v1")
@RequiredArgsConstructor
public class ExternalApiController {
    private final EmployeeService employeeService;
    private final OvertimeService overtimeService;
    private final ResourceAllocationService resourceAllocationService;
    private final DictService dictService;
    private final FileStorage fileStorage;

    @Operation(summary = "Get basic information about employees")
    @GetMapping("/employees")
    public Flux<EmployeeDto> employees(@RequestParam(defaultValue = "false") boolean includeFired) {
        return AuthHandler.currentAuth().flatMapMany(auth -> employeeService.findAll(auth, includeFired)
                .map(employee -> {
                    employee.setHasAvatar(fileStorage.fileExists("avatars", employee.getId() + ".png"));
                    return employee;
                }));
    }

    @Operation(summary = "Get overtime summary for a calendar month")
    @GetMapping("/overtimes/{period}")
    public Flux<OvertimeEmployeeSummary> overtimes(
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        var internalPeriod = period.getYear() * 100 + period.getMonthValue() - 1;
        return AuthHandler.currentAuth().flatMapMany(auth -> overtimeService.getSummary(internalPeriod, auth));
    }

    @Operation(summary = "Get annual resource allocation analytics")
    @GetMapping("/resource-allocations/analytics/{year}")
    public Mono<ResourceAllocationAnalyticsDto> resourceAllocations(@PathVariable int year) {
        return AuthHandler.currentAuth().flatMap(auth -> resourceAllocationService.getAnalytics(year, auth));
    }

    @Operation(summary = "Get basic information about projects")
    @GetMapping("/projects")
    public Flux<ProjectDictDto> projects() {
        return AuthHandler.currentAuth().flatMapMany(dictService::findProjects);
    }
}
