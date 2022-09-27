package ru.abondin.hreasy.platform.service.admin.ba;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountEntry;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountHistoryRepo;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;
import ru.abondin.hreasy.platform.service.admin.ba.dto.CreateOrUpdateBABody;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountMapper;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBusinessAccountService {

    private final BusinessAccountRepo baRepo;
    private final BusinessAccountHistoryRepo baHistoryRepo;
    private final BusinessAccountMapper mapper;
    private final AdminBaSecurityValidator securityValidator;
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
        return securityValidator.validateAddOrUpdateBusinessAccount(auth).flatMap((s) ->
                doUpdate(auth.getEmployeeInfo().getEmployeeId(), now, new BusinessAccountEntry(), body));
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
        var currentEmployeeId = auth.getEmployeeInfo().getEmployeeId();
        return securityValidator.validateAddOrUpdateBusinessAccount(auth)
                .flatMap(s -> baRepo.findById(baId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(baId))))
                .flatMap(entry -> doUpdate(currentEmployeeId, now, entry, body));

    }

    private Mono<Integer> doUpdate(int currentEmployeeId, OffsetDateTime now, BusinessAccountEntry entry, CreateOrUpdateBABody body) {
        entry.setName(body.getName());
        entry.setDescription(body.getDescription());
        entry.setArchived(body.isArchived());
        entry.setCreatedBy(currentEmployeeId);
        entry.setCreatedAt(now);
        return baRepo.save(entry).flatMap(persisted -> {
            var history = mapper.history(persisted);
            history.setUpdatedAt(now);
            history.setUpdatedBy(currentEmployeeId);
            return baHistoryRepo.save(history).map(e -> persisted.getId());
        });
    }
}