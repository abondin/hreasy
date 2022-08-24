package ru.abondin.hreasy.platform.api.admin;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.manager.ManagerService;
import ru.abondin.hreasy.platform.service.admin.manager.dto.CreateManagerBody;
import ru.abondin.hreasy.platform.service.admin.manager.dto.ManagerDto;
import ru.abondin.hreasy.platform.service.admin.manager.dto.UpdateManagerBody;

@RestController()
@RequestMapping("/api/v1/admin/managers")
@RequiredArgsConstructor
@Slf4j
public class AdminManagerController {
    private final ManagerService service;

    @GetMapping
    public Flux<ManagerDto> all() {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.all(auth));
    }

    @GetMapping("/object/{objectType}/{objectId}")
    public Flux<ManagerDto> byObject(@PathVariable String objectType, @PathVariable int objectId) {
        return AuthHandler.currentAuth().flatMapMany(auth -> service.byObject(auth, objectType, objectId));
    }

    @PostMapping
    public Mono<Integer> create(@RequestBody CreateManagerBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.create(auth, body));
    }

    @PutMapping("/{managerId}")
    public Mono<Integer> update(@PathVariable int managerId, @RequestBody UpdateManagerBody body) {
        return AuthHandler.currentAuth().flatMap(auth -> service.update(auth, managerId, body));
    }

    @DeleteMapping("/{managerId}")
    public Mono<Void> delete(@PathVariable int managerId) {
        return AuthHandler.currentAuth().flatMap(auth -> service.delete(auth, managerId));
    }
}
