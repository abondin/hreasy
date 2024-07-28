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
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.udr.dto.AddToJuniorRegistryBody;
import ru.abondin.hreasy.platform.service.udr.dto.JuniorDto;
import ru.abondin.hreasy.platform.service.udr.dto.JuniorReportMapper;
import ru.abondin.hreasy.platform.service.udr.dto.UpdateJuniorRegistryBody;

@Slf4j
@Service
@RequiredArgsConstructor
public class JuniorRegistryService {

    private final DateTimeService dateTimeService;
    private final JuniorRepo juniorRepo;
    private final JuniorSecurityValidator securityValidator;
    private final JuniorReportMapper mapper;
    private final HistoryDomainService history;


    @Transactional
    public Flux<JuniorDto> juniors(AuthContext authContext) {
        return securityValidator
                .get(authContext)
                .flatMapMany(mode -> {
                            switch (mode) {
                                case ALL:
                                    log.debug("Get all juniors by {}", authContext.getUsername());
                                    return juniorRepo.findAllDetailed();
                                case MY:
                                    log.debug("Get own juniors by {}", authContext.getUsername());
                                    return juniorRepo.findAllByBaProjectOrMentorSafe(
                                            authContext.getEmployeeInfo().getAccessibleBas(),
                                            authContext.getEmployeeInfo().getAccessibleProjects(),
                                            authContext.getEmployeeInfo().getEmployeeId());
                                default:
                                    throw new IllegalStateException("Unexpected value: " + mode);
                            }
                        }
                ).map(mapper::toDto);
    }

    @Transactional
    public Mono<Integer> addToRegistry(AuthContext auth, AddToJuniorRegistryBody body) {
        log.info("Adding {} to junior registry by {}", body.getJuniorId(), auth.getUsername());
        var now = dateTimeService.now();
        return securityValidator
                .add(auth)
                .flatMap(r -> juniorRepo.save(mapper.toEntry(body.getJuniorId(), body, auth.getEmployeeInfo().getEmployeeId(), now)))
                .flatMap(entry -> history.persistHistory(entry.getId(), HistoryDomainService.HistoryEntityType.JUNIOR_REGISTRY, entry, now
                        , auth.getEmployeeInfo().getEmployeeId()))
                .map(HistoryEntry::getEntityId);
    }

    @Transactional
    public Mono<Integer> delete(AuthContext auth, int junior) {
        log.info("Removing junior {} from registry by {}", junior, auth.getUsername());
        var now = dateTimeService.now();
        return juniorRepo.findById(junior)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound("Junior", junior))
                .flatMap(entry -> securityValidator.delete(auth, entry)
                        .flatMap(v -> {
                            entry.setDeletedAt(now);
                            entry.setDeletedBy(auth.getEmployeeInfo().getEmployeeId());
                            entry.setNew(false);
                            return juniorRepo.save(entry)
                                    .flatMap(updated -> history.persistHistory(updated.getId(), HistoryDomainService.HistoryEntityType.JUNIOR_REGISTRY, updated, now, auth.getEmployeeInfo().getEmployeeId()))
                                    .map(HistoryEntry::getEntityId);
                        }));
    }

    @Transactional
    public Mono<Integer> update(AuthContext auth, int junior, UpdateJuniorRegistryBody body) {
        log.info("Updating junior {} in registry by {}", junior, auth.getUsername());
        var now = dateTimeService.now();
        return juniorRepo.findById(junior)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound("Junior", junior))
                .flatMap(entry -> securityValidator.update(auth, entry)
                        .flatMap(v -> {
                            mapper.apply(entry, body);
                            return juniorRepo.save(entry)
                                    .flatMap(updated -> history.persistHistory(updated.getId(), HistoryDomainService.HistoryEntityType.JUNIOR_REGISTRY, updated, now, auth.getEmployeeInfo().getEmployeeId()))
                                    .map(HistoryEntry::getEntityId);
                        }));
    }
}
