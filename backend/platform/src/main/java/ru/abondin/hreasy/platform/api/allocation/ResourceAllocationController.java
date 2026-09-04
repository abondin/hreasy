package ru.abondin.hreasy.platform.api.allocation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.allocation.ResourceAllocationService;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSaveBody;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationAnalyticsDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationProjectInputDto;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto;

/**
 * HTTP API for allocation analytics, annual project input, and period closing.
 */
@RestController
@RequestMapping("/api/v1/resource-allocations")
@RequiredArgsConstructor
public class ResourceAllocationController {
    private final ResourceAllocationService service;

    /**
     * Returns the allocation matrix for one zero-based {@code YYYYMM} period.
     */
    @GetMapping("/{period}")
    public Mono<ResourceAllocationSheetDto> getSheet(@PathVariable int period) {
        return AuthHandler.currentAuth().flatMap(auth -> service.getSheet(period, auth));
    }

    /**
     * Returns all non-empty allocation cells for one calendar year.
     */
    @GetMapping("/analytics/{year}")
    public Mono<ResourceAllocationAnalyticsDto> getAnalytics(@PathVariable int year) {
        return AuthHandler.currentAuth().flatMap(auth -> service.getAnalytics(year, auth));
    }

    /**
     * Returns all twelve months of one managed project and calendar year.
     */
    @GetMapping("/input/{year}")
    public Mono<ResourceAllocationProjectInputDto> getProjectInput(@PathVariable int year,
                                                                   @RequestParam(required = false) Integer projectId) {
        return AuthHandler.currentAuth().flatMap(auth -> service.getProjectInput(year, projectId, auth));
    }

    /**
     * Saves changed months of one project/year as one transactional revision.
     */
    @PutMapping("/input/{year}/{projectId}")
    public Mono<Integer> save(@PathVariable int year, @PathVariable int projectId,
                              @Valid @RequestBody ResourceAllocationSaveBody request) {
        return AuthHandler.currentAuth().flatMap(auth -> service.save(year, projectId, request, auth));
    }

    /**
     * Closes one month for allocation editing.
     */
    @PutMapping("/closed-periods/{period}")
    public Mono<Integer> closePeriod(@PathVariable int period, @RequestBody(required = false) PeriodCommentBody body) {
        var comment = body == null ? null : body.comment();
        return AuthHandler.currentAuth().flatMap(auth -> service.closePeriod(period, comment, auth));
    }

    /**
     * Reopens one month for allocation editing.
     */
    @DeleteMapping("/closed-periods/{period}")
    public Mono<Void> reopenPeriod(@PathVariable int period) {
        return AuthHandler.currentAuth().flatMap(auth -> service.reopenPeriod(period, auth));
    }

    public record PeriodCommentBody(String comment) {
    }
}
