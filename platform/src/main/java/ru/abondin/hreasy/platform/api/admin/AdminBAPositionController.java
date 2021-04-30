package ru.abondin.hreasy.platform.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.ba.AdminBAPositionService;
import ru.abondin.hreasy.platform.service.admin.ba.dto.BusinessAccountPositionWithRateDto;
import ru.abondin.hreasy.platform.service.admin.ba.dto.CreateOrUpdateBAPositionBody;

/**
 * Helps project managers to share human resources between different projects.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/business_account/positions/{baId}")
public class AdminBAPositionController {

    private final AdminBAPositionService adminService;

    @GetMapping
    public Flux<BusinessAccountPositionWithRateDto> allPositions(@PathVariable int baId) {
        return AuthHandler.currentAuth().flatMapMany(auth -> adminService.allPositions(auth, baId));
    }

    @PostMapping
    public Mono<Integer> createPosition(@PathVariable int baId, @RequestBody CreateOrUpdateBAPositionBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> adminService.createPosition(auth, baId, body));
    }

    @PutMapping("/{positionId}")
    public Mono<Integer> updatePosition(@PathVariable int baId, @PathVariable int positionId, @RequestBody CreateOrUpdateBAPositionBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> adminService.updatePosition(auth, baId, positionId, body));
    }

    @DeleteMapping("/{positionId}")
    public Mono<Integer> archivePosition(@PathVariable int baId, @PathVariable int positionId) {
        return AuthHandler.currentAuth().flatMap(auth -> adminService.archivePosition(auth, baId, positionId));
    }


}
