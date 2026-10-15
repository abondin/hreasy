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
import ru.abondin.hreasy.platform.service.external.ExternalApiService;
import ru.abondin.hreasy.platform.service.external.dto.ExternalApiDto.*;

import java.time.YearMonth;

/** Minimal read-only integration API using business keys instead of internal IDs. */
@Tag(name = "External API", description = "Read-only API. Requires an opaque Bearer token bound to an acting user.")
@SecurityScheme(name = "externalBearer", type = SecuritySchemeType.HTTP, scheme = "bearer",
        bearerFormat = "opaque", description = "HR Easy-generated opaque token. Not a JWT.")
@SecurityRequirement(name = "externalBearer")
@ApiResponses({
        @ApiResponse(responseCode = "401", description = "Missing or invalid token, or unavailable acting user.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusinessErrorDto.class))),
        @ApiResponse(responseCode = "403", description = "Acting user lacks access. Load-balancer errors may use its own response format.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusinessErrorDto.class))),
        @ApiResponse(responseCode = "400", description = "Invalid request parameter.",
                content = @Content(mediaType = "application/json", schema = @Schema(implementation = BusinessErrorDto.class)))
})
@RestController
@RequestMapping("/external/api/v1")
@RequiredArgsConstructor
public class ExternalApiController {
    private final ExternalApiService service;

    @Operation(operationId = "externalListEmployees", summary = "Get active employee profiles",
            description = "Email is the external employee key, exactly as stored. Dismissed employees, office information, skills and ratings are not exported. There is no includeFired option.")
    @ApiResponse(responseCode = "200", description = "Active employees.", content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = Employee.class))))
    @GetMapping("/employees")
    public Flux<Employee> employees() {
        return AuthHandler.currentAuth().flatMapMany(service::employees);
    }

    @Operation(operationId = "externalGetEmployeeAvatarByEmail", summary = "Download an active employee avatar by email",
            description = "Matches the complete email without case sensitivity, ignoring surrounding whitespace. URL-encode the email, including plus signs. Dismissed employees return 404; no fallback image is returned.")
    @ApiResponse(responseCode = "200", description = "Employee avatar PNG.", content = @Content(mediaType = "image/png",
            schema = @Schema(type = "string", format = "binary")))
    @ApiResponse(responseCode = "404", description = "Active employee or avatar not found.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BusinessErrorDto.class)))
    @GetMapping(value = "/employees/avatar", produces = MediaType.IMAGE_PNG_VALUE)
    public Mono<Resource> avatarByEmail(@RequestParam String email) {
        return AuthHandler.currentAuth().flatMap(auth -> service.avatar(email, auth));
    }

    @Operation(operationId = "externalGetOvertimeSummary", summary = "Get overtime summary for a calendar month",
            description = "Requires overtime_view. Includes historical records of dismissed employees, identified only by email. Items sharing date, project and configured workstream external key are summed. No internal IDs are exposed.")
    @ApiResponse(responseCode = "200", description = "Monthly overtime reports.", content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = Overtime.class))))
    @GetMapping("/overtimes/{period}")
    public Flux<Overtime> overtimes(
            @Parameter(description = "Calendar month in ISO YYYY-MM format.", example = "2026-09")
            @PathVariable @DateTimeFormat(pattern = "yyyy-MM") YearMonth period) {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.overtimes(period, auth));
    }

    @Operation(operationId = "externalGetAllocationAnalytics", summary = "Get annual resource allocations",
            description = "Requires resource_allocation_read and preserves employee-based project visibility. Includes historical records of dismissed employees by email only. Periods use YYYY-MM. Explicit zeros are included; absent cells mean no allocation. Reused workstream external keys are summed. Replace the imported annual snapshot after a complete response; there is no revision feed or closed-period state.")
    @ApiResponse(responseCode = "200", description = "Annual allocation snapshot.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = Allocations.class)))
    @ApiResponse(responseCode = "422", description = "Year is outside the supported calendar range.", content = @Content(mediaType = "application/json",
            schema = @Schema(implementation = BusinessErrorDto.class)))
    @GetMapping("/resource-allocations/analytics/{year}")
    public Mono<Allocations> resourceAllocations(@PathVariable int year) {
        return AuthHandler.currentAuth().flatMap(auth -> service.allocations(year, auth));
    }

    @Operation(operationId = "externalListProjects", summary = "Get projects and active workstreams",
            description = "Includes inactive projects. Projects, workstreams and business accounts contain names and external keys; unconfigured keys are null. Workstream keys are scoped to a project.")
    @ApiResponse(responseCode = "200", description = "Project dictionary.", content = @Content(mediaType = "application/json",
            array = @ArraySchema(schema = @Schema(implementation = Project.class))))
    @GetMapping("/projects")
    public Flux<Project> projects() {
        return AuthHandler.currentAuth().flatMapMany(service::projects);
    }
}
