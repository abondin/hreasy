package ru.abondin.hreasy.platform.api.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.ba.AdminBaAssignmentService;
import ru.abondin.hreasy.platform.service.admin.ba.dto.BusinessAccountAssignmentDto;
import ru.abondin.hreasy.platform.service.admin.ba.dto.CreateBusinessAccountAssignmentBody;

/**
 * Controller to plan business account budgeting
 */
@RestController
@RequiredArgsConstructor
@Slf4j
@RequestMapping("/api/v1/admin/business_account/assignment")
public class AdminBaAssignmentController {

    private final AdminBaAssignmentService service;


    @GetMapping("/{baId}/{period}")
    public Flux<BusinessAccountAssignmentDto> assignments(@PathVariable int baId, @PathVariable int period) {
        return AuthHandler.currentAuth().flatMapMany(auth ->
                service.findInBusinessAccount(auth, baId, period));
    }

    @PostMapping("/{baId}/{period}")
    public Mono<Integer> create(@PathVariable int baId,
                                                     @PathVariable int period,
                                                     @RequestBody CreateBusinessAccountAssignmentBody body) {
        return AuthHandler.currentAuth().flatMap(auth ->
                service.create(auth, baId, period, body));
    }

}
