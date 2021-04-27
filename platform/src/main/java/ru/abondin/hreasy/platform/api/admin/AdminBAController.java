package ru.abondin.hreasy.platform.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.ba.AdminBusinessAccountService;
import ru.abondin.hreasy.platform.service.admin.ba.dto.CreateOrUpdateBABody;
import ru.abondin.hreasy.platform.service.ba.BusinessAccountService;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountDto;

/**
 * Helps project managers to share human resources between different projects.
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/business_account")
public class AdminBAController {

    private final BusinessAccountService baService;
    private final AdminBusinessAccountService adminService;

    @GetMapping
    public Flux<BusinessAccountDto> all(@RequestParam boolean includeArchived) {
        return baService.findAll(includeArchived);
    }

    @PostMapping
    public Mono<Integer> create(@RequestBody CreateOrUpdateBABody body) {
        return AuthHandler.currentAuth().flatMap(auth -> adminService.create(auth, body));
    }

    @PutMapping("/{baId}")
    public Mono<Integer> create(@PathVariable int baId, @RequestBody CreateOrUpdateBABody body) {
        return AuthHandler.currentAuth().flatMap(auth -> adminService.update(auth, baId, body));
    }

    @DeleteMapping("/{baId}")
    public Mono<Integer> arhive(@PathVariable int baId) {
        return AuthHandler.currentAuth().flatMap(auth -> adminService.archive(auth, baId));
    }

}
