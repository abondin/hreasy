package ru.abondin.hreasy.platform.service.admin;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountPositionEntry;
import ru.abondin.hreasy.platform.repo.ba.BusinessAccountPositionRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.admin.ba.dto.BusinessAccountPositionWithRateDto;
import ru.abondin.hreasy.platform.service.admin.ba.dto.CreateOrUpdateBAPositionBody;
import ru.abondin.hreasy.platform.service.ba.dto.BusinessAccountMapper;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminBAPositionService {

    private final BusinessAccountPositionRepo positionRepo;
    private final BusinessAccountMapper mapper;
    private final AdminSecurityValidator securityValidator;
    private final DateTimeService dateTimeService;

    public Flux<BusinessAccountPositionWithRateDto> allPositions(AuthContext auth, int businessAccountId, boolean includeArchived) {
        return securityValidator.validateGetBusinessAccountDetailed(auth)
                .flatMapMany((s) ->
                        (includeArchived ? positionRepo.findAll(businessAccountId) : positionRepo.findNotArchived(businessAccountId)))
                .map(mapper::toPositionWithRate);
    }

    /**
     * Create new business accounts.
     *
     * @return
     */
    @Transactional
    public Mono<Integer> create(AuthContext auth, int businessAccountId, CreateOrUpdateBAPositionBody body) {
        log.info("Create new position {} for ba {} by {}", body, businessAccountId, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateAddOrUpdateBusinessAccount(auth).flatMap((s) -> {
            var entry = new BusinessAccountPositionEntry();
            entry.setCreatedAt(now);
            entry.setCreatedBy(auth.getEmployeeInfo().getEmployeeId());
            entry.setDescription(body.getDescription());
            entry.setName(body.getName());
            entry.setBusinessAccount(body.getBusinessAccountId());
            return positionRepo.save(entry);
        }).map(e -> e.getId());
    }

    /**
     * Update business account position
     *
     * @return
     */
    @Transactional
    public Mono<Integer> update(AuthContext auth, int baId, int positionId, CreateOrUpdateBAPositionBody body) {
        log.info("Update business account position {} for ba {} with {} by {}", positionId, baId, body, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateAddOrUpdateBusinessAccount(auth)
                .flatMap(s -> positionRepo.findById(positionId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(baId))))
                .flatMap(entry -> {
                    entry.setDescription(body.getDescription());
                    entry.setName(body.getName());
                    entry.setBusinessAccount(body.getBusinessAccountId());
                    return positionRepo.save(entry);
                })
                .map(e -> e.getId());
    }

    /**
     * Archive business account position
     *
     * @return
     */
    @Transactional
    public Mono<Integer> archive(AuthContext auth, int baId, int positionId) {
        log.info("Archive business account position {} for ba {} by {}", positionId, baId, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator.validateAddOrUpdateBusinessAccount(auth)
                .flatMap(s -> positionRepo.findById(baId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(baId))))
                .flatMap(entry -> {
                    entry.setArchivedBy(auth.getEmployeeInfo().getEmployeeId());
                    entry.setArchivedAt(now);
                    return positionRepo.save(entry);
                })
                .map(e -> e.getId());
    }
}
