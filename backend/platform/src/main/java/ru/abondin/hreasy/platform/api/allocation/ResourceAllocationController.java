package ru.abondin.hreasy.platform.api.allocation;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.allocation.ResourceAllocationService;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSaveBody;
import ru.abondin.hreasy.platform.service.allocation.dto.ResourceAllocationSheetDto;

/**
 * HTTP API for viewing and saving monthly resource allocations.
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
     * Saves changed cells as one transactional allocation revision.
     */
    @PutMapping("/{period}")
    public Mono<Integer> save(@PathVariable int period,
                              @Valid @RequestBody ResourceAllocationSaveBody request) {
        return AuthHandler.currentAuth().flatMap(auth -> service.save(period, request, auth));
    }
}
