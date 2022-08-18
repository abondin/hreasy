package ru.abondin.hreasy.platform.service.admin.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.manager.ManagerRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.manager.dto.ManagerDto;
import ru.abondin.hreasy.platform.service.admin.manager.dto.ManagerMapper;

/**
 * Admin managers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerService {

    private final ManagerRepo repo;
    private final ManagerMapper mapper;
    private final ManagerSecurityValidator securityValidator;
    private final DateTimeService dateTimeService;

    public Flux<ManagerDto> all(AuthContext auth) {
        var now = dateTimeService.now();
        return securityValidator.validateAdminManagers(auth)
                .flatMapMany(valid -> repo.findDetailed(now))
                .map(mapper::fromEntry);
    }
}
