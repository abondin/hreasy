package ru.abondin.hreasy.platform.service.udr;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessErrorFactory;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.config.udr.UdrProps;
import ru.abondin.hreasy.platform.repo.history.HistoryEntry;
import ru.abondin.hreasy.platform.repo.udr.JuniorEntry;
import ru.abondin.hreasy.platform.repo.udr.JuniorRepo;
import ru.abondin.hreasy.platform.repo.udr.JuniorReportEntry;
import ru.abondin.hreasy.platform.repo.udr.JuniorReportRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.udr.dto.*;

import java.util.function.Consumer;

@Slf4j
@Service
@RequiredArgsConstructor
public class JuniorRegistryService {

    public static final String JUNIOR_LOG_ENTITY_TYPE = "Junior";
    public static final String JUNIOR_REPORT_LOG_ENTITY_TYPE = "JuniorReport";
    private final DateTimeService dateTimeService;
    private final JuniorRepo juniorRepo;
    private final JuniorReportRepo reportRepo;
    private final JuniorSecurityValidator securityValidator;
    private final JuniorRegistryMapper mapper;
    private final HistoryDomainService history;
    private final UdrProps props;


    @Transactional(readOnly = true)
    public Flux<JuniorDto> juniors(AuthContext authContext) {
        var now = dateTimeService.now();
        var currentEmployee=authContext.getEmployeeInfo().getEmployeeId();
        return securityValidator
                .get(authContext)
                .flatMapMany(mode -> {
                            switch (mode) {
                                case ALL:
                                    log.debug("Get all juniors by {}", authContext.getUsername());
                                    return juniorRepo.findAllDetailed(now, currentEmployee);
                                case MY:
                                    log.debug("Get own juniors by {}", authContext.getUsername());
                                    return juniorRepo.findAllByBaProjectOrMentorSafe(
                                            authContext.getEmployeeInfo().getAccessibleBas(),
                                            authContext.getEmployeeInfo().getAccessibleDepartments(),
                                            authContext.getEmployeeInfo().getAccessibleProjects(),
                                            authContext.getEmployeeInfo().getEmployeeId(),
                                            now, currentEmployee);
                                default:
                                    throw new IllegalStateException("Unexpected value: " + mode);
                            }
                        }
                ).map(e->mapper.toDto(e, now, props));
    }


    @Transactional(readOnly = true)
    public Mono<JuniorDto> juniorDetailed(AuthContext auth, int registryId) {
        log.debug("Get junior details {} by {}", registryId, auth.getUsername());
        var now = dateTimeService.now();
        var currentEmployee=auth.getEmployeeInfo().getEmployeeId();
        return securityValidator
                .get(auth)
                .flatMap(mode -> {
                            switch (mode) {
                                case ALL:
                                    return juniorRepo.findDetailed(registryId, now, currentEmployee);
                                case MY:
                                    return juniorRepo.findDetailedByBaProjectOrMentorSafe(
                                            registryId,
                                            auth.getEmployeeInfo().getAccessibleBas(),
                                            auth.getEmployeeInfo().getAccessibleDepartments(),
                                            auth.getEmployeeInfo().getAccessibleProjects(),
                                            auth.getEmployeeInfo().getEmployeeId(),
                                            now, currentEmployee);
                                default:
                                    throw new IllegalStateException("Unexpected value: " + mode);
                            }
                        }
                ).switchIfEmpty(BusinessErrorFactory.entityNotFound(JUNIOR_LOG_ENTITY_TYPE, registryId))
                .map(e->mapper.toDto(e, now, props));
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
        return doUpdate(auth, registryId, entry -> mapper.apply(entry, body));
    }

    @Transactional
    public Mono<Integer> graduate(AuthContext auth, int registryId, GraduateJuniorBody body) {
        log.info("Graduating junior {} by {}", registryId, auth.getUsername());
        return doUpdate(auth, registryId, entry -> mapper.apply(entry, body, auth.getEmployeeInfo().getEmployeeId(), dateTimeService.now()));
    }

    @Transactional
    public Mono<Integer> cancelGraduation(AuthContext auth, int registryId) {
        log.info("Cancel graduation for junior {} by {}", registryId, auth.getUsername());
        return doUpdate(auth, registryId, entry -> {
            entry.setGraduatedAt(null);
            entry.setGraduatedBy(null);
            entry.setGraduatedComment(null);
        });
    }

    @Transactional
    public Mono<Integer> addJuniorReport(AuthContext auth, int registryId, AddJuniorReportBody body) {
        log.info("Add report to junior {} by {}", registryId, auth.getUsername());
        return juniorRepo.findById(registryId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(JUNIOR_LOG_ENTITY_TYPE, registryId))
                .flatMap(entry -> securityValidator.addReport(auth, entry))
                .flatMap(v -> {
                    var reportEntry = mapper.toReportEntry(body, registryId, auth.getEmployeeInfo().getEmployeeId(), dateTimeService.now());
                    return reportRepo.save(reportEntry);
                }).flatMap(persisted ->
                        history.persistHistory(persisted.getId(),
                                HistoryDomainService.HistoryEntityType.JUNIOR_REGISTRY_REPORT,
                                persisted, dateTimeService.now(), auth.getEmployeeInfo().getEmployeeId()))
                .map(HistoryEntry::getEntityId);

    }

    @Transactional
    public Mono<Integer> updateJuniorReport(AuthContext auth, int registryId, int reportId, UpdateJuniorReportBody body) {
        log.info("Update report {} of junior {} by {}", reportId, registryId, auth.getUsername());
        return doUpdateReport(auth, registryId, registryId, entry -> mapper.applyReportUpdate(entry, body));
    }

    @Transactional
    public Mono<Integer> deleteJuniorReport(AuthContext auth, int registryId, int reportId) {
        log.info("Delete report {} of junior {} by {}", reportId, registryId, auth.getUsername());
        return doUpdateReport(auth, registryId, reportId, entry -> {
            entry.setDeletedAt(dateTimeService.now());
            entry.setDeletedBy(auth.getEmployeeInfo().getEmployeeId());
        });
    }


    private Mono<Integer> doUpdate(AuthContext auth, int registryId, Consumer<JuniorEntry> changesApplier) {
        var now = dateTimeService.now();
        return juniorRepo.findById(registryId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(JUNIOR_LOG_ENTITY_TYPE, registryId))
                .flatMap(entry -> securityValidator.update(auth, entry)
                        .flatMap(v -> {
                            changesApplier.accept(entry);
                            return juniorRepo.save(entry)
                                    .flatMap(updated -> history.persistHistory(updated.getId(), HistoryDomainService.HistoryEntityType.JUNIOR_REGISTRY, updated, now, auth.getEmployeeInfo().getEmployeeId()))
                                    .map(HistoryEntry::getEntityId);
                        }));
    }

    private Mono<Integer> doUpdateReport(AuthContext auth, int juniorId, int reportId, Consumer<JuniorReportEntry> changesApplier) {
        var now = dateTimeService.now();
        return reportRepo.findByJuniorIdAndReportId(juniorId, reportId)
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(JUNIOR_REPORT_LOG_ENTITY_TYPE, reportId))
                .flatMap(reportEntry -> securityValidator.updateReport(auth, reportEntry)
                        .flatMap(v -> {
                            changesApplier.accept(reportEntry);
                            return reportRepo.save(reportEntry)
                                    .flatMap(updated -> history.persistHistory(updated.getId(), HistoryDomainService.HistoryEntityType.JUNIOR_REGISTRY_REPORT, updated, now, auth.getEmployeeInfo().getEmployeeId()))
                                    .map(HistoryEntry::getEntityId);
                        }));
    }


}
