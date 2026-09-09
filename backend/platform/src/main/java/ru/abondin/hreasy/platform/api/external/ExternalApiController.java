package ru.abondin.hreasy.platform.api.external;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.api.BusinessErrorDto;
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
@Tag(name = "External API", description = "Read-only system-to-system API. Requires an opaque Bearer token and an allowed source IP at nginx.")
@SecurityScheme(name = "externalBearer", type = SecuritySchemeType.HTTP, scheme = "bearer",
        bearerFormat = "opaque", description = "HR Easy-generated opaque token bound to an acting user. Not a JWT.")
@SecurityRequirement(name = "externalBearer")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Missing or invalid token, or unavailable acting user.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusinessErrorDto.class))),
        @ApiResponse(responseCode = "403", description = "Acting user lacks access, or nginx rejects the source IP. Nginx may return HTML.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusinessErrorDto.class)))
})
@RestController
@RequestMapping("/external/api/v1")
@RequiredArgsConstructor
public class ExternalApiController {
    private final EmployeeService employeeService;
    private final OvertimeService overtimeService;
    private final ResourceAllocationService resourceAllocationService;
    private final DictService dictService;
    private final FileStorage fileStorage;

    @Operation(operationId = "externalListEmployees", summary = "Get basic information about employees",
            description = "Returns active employees by default. Role and skill visibility follows the acting user's permissions. "
                    + "The response contains HR Easy IDs and email, but no external ERP employee ID.")
    @ApiResponse(responseCode = "200", description = "Employee list.", content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = EmployeeDto.class))))
    @ApiResponse(responseCode = "400", description = "Invalid includeFired boolean value.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BusinessErrorDto.class)))
    @GetMapping("/employees")
    public Flux<EmployeeDto> employees(
            @Parameter(description = "Include dismissed employees.", example = "false")
            @RequestParam(defaultValue = "false") boolean includeFired) {
        return AuthHandler.currentAuth().flatMapMany(auth -> employeeService.findAll(auth, includeFired)
                .map(employee -> {
                    employee.setHasAvatar(fileStorage.fileExists("avatars", employee.getId() + ".png"));
                    return employee;
                }));
    }

    @Operation(operationId = "externalGetEmployeeAvatar", summary = "Download employee avatar by ID",
            description = "Returns the actual PNG avatar, including for dismissed employees. "
                    + "Uses the same authenticated access as the employee list. No fallback image is returned.")
    @ApiResponse(responseCode = "200", description = "Employee avatar PNG.", content = @Content(mediaType = "image/png",
            schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "400", description = "Employee ID is not a valid integer.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BusinessErrorDto.class)))
    @ApiResponse(responseCode = "404", description = "Employee or avatar not found.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BusinessErrorDto.class)))
    @GetMapping(value = "/employees/{employeeId}/avatar", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<Resource> avatar(
            @Parameter(description = "HR Easy employee identifier.", required = true, example = "101")
            @PathVariable int employeeId) {
        return AuthHandler.currentAuth().flatMap(auth -> employeeService.avatar(employeeId, auth));
    }

    @Operation(operationId = "externalGetEmployeeAvatarByEmail", summary = "Download employee avatar by email",
            description = "Matches the complete email without case sensitivity, ignoring surrounding whitespace. "
                    + "Includes dismissed employees and returns no fallback image. URL-encode the email, including any plus sign.")
    @ApiResponse(responseCode = "200", description = "Employee avatar PNG.", content = @Content(mediaType = "image/png",
            schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "400", description = "Email parameter is missing or blank.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BusinessErrorDto.class)))
    @ApiResponse(responseCode = "404", description = "Employee or avatar not found.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BusinessErrorDto.class)))
    @GetMapping(value = "/employees/avatar", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<Resource> avatarByEmail(
            @Parameter(description = "Employee email, matched exactly without case sensitivity.", required = true,
                    example = "alex.morgan@example.test", schema = @Schema(type = "string", format = "email"))
            @RequestParam String email) {
        return AuthHandler.currentAuth().flatMap(auth -> employeeService.avatarByEmail(email, auth));
    }

    @Operation(operationId = "externalGetOvertimeSummary", summary = "Get overtime summary for a calendar month",
            description = "Requires overtime_view. Returns employee report totals and approval statuses. "
                    + "Items aggregate hours by date and project across all workstreams.")
    @ApiResponse(responseCode = "200", description = "Monthly overtime summaries.", content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = OvertimeEmployeeSummary.class))))
    @ApiResponse(responseCode = "400", description = "Invalid calendar month.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BusinessErrorDto.class)))
    @GetMapping("/overtimes/{period}")
    public Flux<OvertimeEmployeeSummary> overtimes(
            @Parameter(description = "Calendar month in ISO YYYY-MM format; month is 01 through 12.",
                    required = true, example = "2026-09", schema = @Schema(type = "string", pattern = "^[0-9]{4}-(0[1-9]|1[0-2])$"))
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        var internalPeriod = period.getYear() * 100 + period.getMonthValue() - 1;
        return AuthHandler.currentAuth().flatMapMany(auth -> overtimeService.getSummary(internalPeriod, auth));
    }

    @Operation(operationId = "externalGetAllocationAnalytics", summary = "Get annual resource allocation analytics",
            description = "Requires resource_allocation_read. Returns employees whose current project is accessible or who have an allocation on an accessible project in this year, with all their annual allocations across projects. Employees without annual allocations are omitted. "
                    + "Explicit zeros are included; absent cells mean no allocation. Periods use zero-based YYYYMM. "
                    + "No write operations, revision feed, or closed-period states are exposed.")
    @ApiResponse(responseCode = "200", description = "Complete annual allocation snapshot.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = ResourceAllocationAnalyticsDto.class)))
    @ApiResponse(responseCode = "400", description = "Year is not a valid integer.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BusinessErrorDto.class)))
    @ApiResponse(responseCode = "422", description = "Year is outside the supported calendar range.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BusinessErrorDto.class)))
    @GetMapping("/resource-allocations/analytics/{year}")
    public Mono<ResourceAllocationAnalyticsDto> resourceAllocations(
            @Parameter(description = "Calendar year to export.", required = true, example = "2026")
            @PathVariable int year) {
        return AuthHandler.currentAuth().flatMap(auth -> resourceAllocationService.getAnalytics(year, auth));
    }

    @Operation(operationId = "externalListProjects", summary = "Get basic information about projects",
            description = "Returns the project dictionary, including inactive projects, optional external IDs, and active workstreams. "
                    + "Join project IDs with allocation cells. Referenced deleted workstreams are supplied by allocation analytics instead.")
    @ApiResponse(responseCode = "200", description = "Project dictionary.", content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = ProjectDictDto.class))))
    @GetMapping("/projects")
    public Flux<ProjectDictDto> projects() {
        return AuthHandler.currentAuth().flatMapMany(dictService::findProjects);
    }
}
