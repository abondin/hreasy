package ru.abondin.hreasy.platform.api.allocation;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.allocation.ResourceAllocationService;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSaveBody;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationProjectInputDto;

import java.util.List;

/**
 * HTTP API for allocation analytics, annual project input, and period closing.
 */
@RestController
@RequestMapping("/api/v1/resource-allocations")
@RequiredArgsConstructor
public class ResourceAllocationController {
    private final ResourceAllocationService service;

    /**
     * Returns all non-empty allocation cells for one calendar year.
     */
    @GetMapping("/analytics/{year}")
    public Mono<ResourceAllocationAnalyticsDto> getAnalytics(@PathVariable int year) {
        return AuthHandler.currentAuth().flatMap(auth -> service.getAnalytics(year, auth));
    }

    /**
     * Returns all twelve months of one project and calendar year.
     */
    @GetMapping("/input/{year}")
    public Mono<ResourceAllocationProjectInputDto> getProjectInput(@PathVariable int year,
                                                                   @RequestParam(required = false) Integer projectId,
                                                                   @RequestParam(required = false) Integer workstreamId) {
        return AuthHandler.currentAuth().flatMap(auth -> service.getProjectInput(year, projectId, workstreamId, auth));
    }

    /**
     * Saves changed months of one project/year as one transactional revision.
     */
    @PutMapping("/input/{year}/{projectId}")
    public Mono<Integer> save(@PathVariable int year, @PathVariable int projectId,
                              @RequestParam(required = false) Integer workstreamId,
                              @Valid @RequestBody ResourceAllocationSaveBody request) {
        return AuthHandler.currentAuth().flatMap(auth -> service.save(year, projectId, workstreamId, request, auth));
    }

    /**
     * Replaces the closed allocation periods for one calendar year.
     */
    @PutMapping("/closed-periods/{year}")
    public Mono<List<Integer>> saveClosedPeriods(@PathVariable int year,
                                                  @Valid @RequestBody ClosedPeriodsBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.saveClosedPeriods(year, body.closedPeriods(), auth));
    }

    /**
     * Returns closed allocation periods for a calendar year.
     */
    @GetMapping("/closed-periods/{year}")
    public Flux<Integer> closedPeriods(@PathVariable int year) {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.getClosedPeriods(year, auth));
    }

    public record ClosedPeriodsBody(@NotNull List<@NotNull Integer> closedPeriods) {
    }
}
