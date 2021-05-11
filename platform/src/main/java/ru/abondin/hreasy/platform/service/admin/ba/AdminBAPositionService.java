package ru.abondin.hreasy.platform.service.admin.ba;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountPositionEntry;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountPositionHistoryRepo;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountPositionRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.AdminSecurityValidator;
import ru.abondin.hreasy.platform.service.admin.ba.dto.BusinessAccountPositionWithRateDto;
import ru.abondin.hreasy.platform.service.admin.ba.dto.CreateOrUpdateBAPositionBody;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountMapper;

import java.time.OffsetDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBAPositionService {

    private final BusinessAccountPositionRepo positionRepo;
    private final BusinessAccountPositionHistoryRepo positionHistoryRepo;
    private final BusinessAccountMapper mapper;
    private final AdminSecurityValidator securityValidator;
    private final DateTimeService dateTimeService;

    public Flux<BusinessAccountPositionWithRateDto> allPositions(AuthContext auth, int businessAccountId) {
        return securityValidator.validateGetBusinessAccountDetailed(auth)
                .flatMapMany((s) -> positionRepo.findAll(businessAccountId))
                .map(mapper::toPositionWithRate);
    }


    /**
     * Create new business account position.
     *
     * @return
     */
    @Transactional
    public Mono<Integer> createPosition(AuthContext auth, int businessAccountId, CreateOrUpdateBAPositionBody body) {
        log.info("Create new position {} for ba {} by {}", body, businessAccountId, auth.getUsername());
        var now = dateTimeService.now();
        var currentEmployeeId = auth.getEmployeeInfo().getEmployeeId();
        return securityValidator.validateUpdateBAPosition(auth, businessAccountId).flatMap((s) ->
                doUpdate(businessAccountId, currentEmployeeId, now, new BusinessAccountPositionEntry(), body));
    }

    /**
     * Update business account position
     *
     * @return
     */
    @Transactional
    public Mono<Integer> updatePosition(AuthContext auth, int baId, int positionId, CreateOrUpdateBAPositionBody body) {
        log.info("Update business account position {} for ba {} with {} by {}", positionId, baId, body, auth.getUsername());
        var currentEmployeeId = auth.getEmployeeInfo().getEmployeeId();
        var now = dateTimeService.now();
        return securityValidator.validateUpdateBAPosition(auth, baId)
                .flatMap(s -> positionRepo.findById(positionId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(baId))))
                .flatMap(entry -> doUpdate(baId, currentEmployeeId, now, entry, body));
    }


    private Mono<Integer> doUpdate(int baId, int currentEmployeeId, OffsetDateTime now,
                                   BusinessAccountPositionEntry entry, CreateOrUpdateBAPositionBody body) {
        entry.setDescription(body.getDescription());
        entry.setName(body.getName());
        entry.setBusinessAccount(baId);
        entry.setRate(body.getRate());
        entry.setArchived(body.isArchived());
        entry.setCreatedAt(now);
        entry.setCreatedBy(currentEmployeeId);
        return positionRepo.save(entry).flatMap(persisted -> {
            var history = mapper.history(persisted);
            history.setUpdatedAt(now);
            history.setUpdatedBy(currentEmployeeId);
            return positionHistoryRepo.save(history).map(e -> persisted.getId());
        });
    }

}
