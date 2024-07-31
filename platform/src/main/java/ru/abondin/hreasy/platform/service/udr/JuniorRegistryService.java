package ru.abondin.hreasy.platform.service.udr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessErrorFactory;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.history.HistoryEntry;
import ru.abondin.hreasy.platform.repo.udr.JuniorRepo;
import ru.abondin.hreasy.platform.repo.udr.JuniorReportRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.udr.dto.AddToJuniorRegistryBody;
import ru.abondin.hreasy.platform.service.udr.dto.JuniorDto;
import ru.abondin.hreasy.platform.service.udr.dto.JuniorRegistryMapper;
import ru.abondin.hreasy.platform.service.udr.dto.UpdateJuniorRegistryBody;

@Slf4j
@Service
@RequiredArgsConstructor
public class JuniorRegistryService {

    public static final String JUNIOR_LOG_ENTITY_TYPE = "Junior";
    private final DateTimeService dateTimeService;
    private final JuniorRepo juniorRepo;
    private final JuniorReportRepo reportRepo;
    private final JuniorSecurityValidator securityValidator;
    private final JuniorRegistryMapper mapper;
    private final HistoryDomainService history;


    @Transactional(readOnly = true)
    public Flux<JuniorDto> juniors(AuthContext authContext) {
        var now = dateTimeService.now();
        return securityValidator
                .get(authContext)
                .flatMapMany(mode -> {
                            switch (mode) {
                                case ALL:
                                    log.debug("Get all juniors by {}", authContext.getUsername());
                                    return juniorRepo.findAllDetailed(now);
                                case MY:
                                    log.debug("Get own juniors by {}", authContext.getUsername());
                                    return juniorRepo.findAllByBaProjectOrMentorSafe(
                                            authContext.getEmployeeInfo().getAccessibleBas(),
                                            authContext.getEmployeeInfo().getAccessibleProjects(),
                                            authContext.getEmployeeInfo().getEmployeeId(),
                                            now);
                                default:
                                    throw new IllegalStateException("Unexpected value: " + mode);
                            }
                        }
                ).map(mapper::toDto);
    }


    @Transactional(readOnly = true)
    public Mono<JuniorDto> juniorDetailed(AuthContext auth, int registryId) {
        log.debug("Get junior details {} by {}", registryId, auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator
                .get(auth)
                .flatMap(mode -> {
                            switch (mode) {
                                case ALL:
                                    return juniorRepo.findDetailed(registryId, now);
                                case MY:
                                    return juniorRepo.findDetailedByBaProjectOrMentorSafe(
                                            registryId,
                                            auth.getEmployeeInfo().getAccessibleBas(),
                                            auth.getEmployeeInfo().getAccessibleProjects(),
                                            auth.getEmployeeInfo().getEmployeeId(),
                                            now);
                                default:
                                    throw new IllegalStateException("Unexpected value: " + mode);
                            }
                        }
                ).switchIfEmpty(BusinessErrorFactory.entityNotFound(JUNIOR_LOG_ENTITY_TYPE, registryId))
                .map(mapper::toDto);
    }

    @Transactional
    public Mono<Integer> addToRegistry(AuthContext auth, AddToJuniorRegistryBody body) {
        log.info("Adding {} to junior registry by {}", body.getJuniorEmplId(), auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator
                .add(auth)
                .flatMap(v -> juniorRepo.save(mapper.toEntry(body, auth.getEmployeeInfo().getEmployeeId(), now)))
                .flatMap(entry -> history.persistHistory(entry.getId(), HistoryDomainService.HistoryEntityType.JUNIOR_REGISTRY, entry, now
                        , auth.getEmployeeInfo().getEmployeeId()))
                .map(HistoryEntry::getEntityId);
    }

    @Transactional
    public Mono<Integer> delete(AuthContext auth, int registryId) {
        log.info("Removing junior registry {} by {}", registryId, auth.getUsername());
        var now = dateTimeService.now();
        // 1. Find junior in registry
        return juniorRepo.findById(registryId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(JUNIOR_LOG_ENTITY_TYPE, registryId))
                // 2. Validate if can delete
                .flatMap(entry -> securityValidator.delete(auth, entry)
                        .flatMap(v -> {
                            entry.setDeletedAt(now);
                            entry.setDeletedBy(auth.getEmployeeInfo().getEmployeeId());
                            // 3. Delete all reports
                            return reportRepo.markAllAsDeleted(registryId, auth.getEmployeeInfo().getEmployeeId(), now)
                                    .then(
                                            // 4. Mark as deleted
                                            juniorRepo.save(entry)
                                                    // 5. Update history
                                                    .flatMap(updated -> history.persistHistory(updated.getId(), HistoryDomainService.HistoryEntityType.JUNIOR_REGISTRY, updated, now, auth.getEmployeeInfo().getEmployeeId()))
                                                    .map(HistoryEntry::getEntityId)
                                    );
                        }));
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int registryId, UpdateJuniorRegistryBody body) {
        log.info("Updating junior registry {} by {}", registryId, auth.getUsername());
        var now = dateTimeService.now();
        return juniorRepo.findById(registryId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(JUNIOR_LOG_ENTITY_TYPE, registryId))
                .flatMap(entry -> securityValidator.update(auth, entry)
                        .flatMap(v -> {
                            mapper.apply(entry, body);
                            return juniorRepo.save(entry)
                                    .flatMap(updated -> history.persistHistory(updated.getId(), HistoryDomainService.HistoryEntityType.JUNIOR_REGISTRY, updated, now, auth.getEmployeeInfo().getEmployeeId()))
                                    .map(HistoryEntry::getEntityId);
                        }));
    }


}
