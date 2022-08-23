package ru.abondin.hreasy.platform.service.admin.manager;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.manager.ManagerRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.admin.manager.dto.CreateManagerBody;
import ru.abondin.hreasy.platform.service.admin.manager.dto.ManagerDto;
import ru.abondin.hreasy.platform.service.admin.manager.dto.ManagerMapper;
import ru.abondin.hreasy.platform.service.admin.manager.dto.UpdateManagerBody;

/**
 * Admin managers
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ManagerService {

    private final ManagerRepo repo;
    private final ManagerMapper mapper;
    private final HistoryDomainService history;
    private final ManagerSecurityValidator securityValidator;
    private final DateTimeService dateTimeService;

    public Flux<ManagerDto> all(AuthContext auth) {
        var now = dateTimeService.now();
        return securityValidator.validateAdminManagers(auth)
                .flatMapMany(valid -> repo.findDetailed(now))
                .map(mapper::fromEntry);
    }

    @Transactional
    public Mono<Integer> create(AuthContext auth, CreateManagerBody body) {
        var createdAt = dateTimeService.now();
        var createdBy = auth.getEmployeeInfo().getEmployeeId();
        log.info("Creating new manager link {} by ", body, auth.getUsername());
        return securityValidator.validateAdminManagers(auth)
                .map(valid -> mapper.toEntry(body, createdAt, createdBy))
                .flatMap(entry -> repo.save(entry))
                .flatMap(persistent -> history.persistHistory(persistent.getId(),
                        HistoryDomainService.HistoryEntityType.MANAGER, persistent,
                        createdAt, createdBy).map(h -> persistent.getId()));
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int managerId, UpdateManagerBody body) {
        var updatedAt = dateTimeService.now();
        var updatedBy = auth.getEmployeeInfo().getEmployeeId();
        log.info("Updating manager link {} by {} with {}", managerId, auth.getUsername(), body);
        return securityValidator.validateAdminManagers(auth)
                .flatMap(valid -> repo.findById(managerId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(managerId))))
                .map(entry -> mapper.update(entry, body, updatedAt, updatedBy))
                .flatMap(entry -> repo.save(entry))
                .flatMap(persistent -> history.persistHistory(persistent.getId(),
                        HistoryDomainService.HistoryEntityType.MANAGER, persistent,
                        updatedAt, updatedBy).map(h -> persistent.getId()));
    }

    @Transactional
    public Mono<? extends Void> delete(AuthContext auth, int managerId) {
        var deletedAt = dateTimeService.now();
        log.info("Deleting manager link {} by {}", managerId, auth.getUsername());
        return securityValidator.validateAdminManagers(auth)
                .flatMap(valid -> history.persistHistory(
                        managerId, HistoryDomainService.HistoryEntityType.MANAGER, null, deletedAt, auth.getEmployeeInfo().getEmployeeId()))
                .flatMap(h -> repo.deleteById(managerId));
    }
}
