package ru.abondin.hreasy.platform.service.admin.ba;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountEntry;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountPositionEntry;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;
import ru.abondin.hreasy.platform.service.admin.ba.dto.CreateOrUpdateBABody;
import ru.abondin.hreasy.platform.service.admin.ba.dto.CreateOrUpdateBAPositionBody;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBusinessAccountService {

    private final BusinessAccountRepo baRepo;
    private final BusinessAccountMapper mapper;
    private final AdminSecurityValidator securityValidator;
    private final DateTimeService dateTimeService;

    /**
     * Create new business accounts.
     *
     * @return
     */
    @Transactional
    public Mono<Integer> create(AuthContext auth, CreateOrUpdateBABody body) {
        log.info("Create new business account {} by {}", body, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateAddOrUpdateBusinessAccount(auth).flatMap((s) -> {
            var entry = new BusinessAccountEntry();
            entry.setCreatedAt(now);
            entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
            entry.setDescription(body.getDescription());
            entry.setName(body.getName());
            entry.setResponsibleEmployee(body.getResponsibleEmployee());
            return baRepo.save(entry);
        }).map(e -> e.getId());
    }

    /**
     * Update business account
     *
     * @return
     */
    @Transactional
    public Mono<Integer> update(AuthContext auth, int baId, CreateOrUpdateBABody body) {
        log.info("Update new business account {} with {} by {}", baId, body, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateAddOrUpdateBusinessAccount(auth)
                .flatMap(s -> baRepo.findById(baId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(baId))))
                .flatMap(entry -> {
                    entry.setName(body.getName());
                    entry.setDescription(body.getDescription());
                    entry.setResponsibleEmployee(body.getResponsibleEmployee());
                    return baRepo.save(entry);
                })
                .map(e -> e.getId());
    }

    /**
     * Archive business account
     *
     * @return
     */
    @Transactional
    public Mono<Integer> archive(AuthContext auth, int baId) {
        log.info("Archive business account {} by {}", baId, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateAddOrUpdateBusinessAccount(auth)
                .flatMap(s -> baRepo.findById(baId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(baId))))
                .flatMap(entry -> {
                    entry.setArchivedBy(auth.getEmployeeInfo().getEmployeeId());
                    entry.setArchivedAt(now);
                    return baRepo.save(entry);
                })
                .map(e -> e.getId());
    }

}
