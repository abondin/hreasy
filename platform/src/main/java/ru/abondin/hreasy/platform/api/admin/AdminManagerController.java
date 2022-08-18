package ru.abondin.hreasy.platform.api.admin;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthHandler;
import ru.abondin.hreasy.platform.service.admin.manager.ManagerService;
import ru.abondin.hreasy.platform.service.admin.manager.dto.ManagerDto;

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
}
