package ru.abondin.hreasy.platform.service.salary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.NonNull;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.BusinessErrorFactory;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.history.HistoryEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestClosedPeriodEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestClosedPeriodRepo;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.salary.dto.*;
import ru.abondin.hreasy.platform.service.salary.dto.link.SalaryRequestLinkCreateBody;
import ru.abondin.hreasy.platform.service.salary.dto.link.SalaryRequestLinkType;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSalaryRequestService {
    private final SalaryRequestRepo requestRepo;

    private final SalaryRequestClosedPeriodRepo closedPeriodRepo;
    private final SalarySecurityValidator secValidator;
    private final SalaryRequestMapper mapper;
    private final SalaryRequestDomainService domainService;

    private final HistoryDomainService historyDomainService;

    private final DateTimeService dateTimeService;


    public Flux<SalaryRequestDto> findAll(AuthContext auth, int periodId) {
        var now = dateTimeService.now();
        log.debug("Get all requests for period {} by {}", periodId, auth);
        return secValidator.validateViewAll(auth)
                .flatMapMany(v -> requestRepo.findAllNotDeleted(periodId, now))
                .map(mapper::fromEntry);
    }

    /**
     * @param auth
     * @param employeeId
     * @return all requests for employee for all periods
     */
    public Flux<SalaryRequestDto> findAllForEmployeeForAllPeriods(AuthContext auth, int employeeId) {
        log.debug("Get all requests for employee {} by {}", employeeId, auth);
        var now = dateTimeService.now();
        return secValidator.validateViewAll(auth)
                .flatMapMany(v -> requestRepo.findAllForEmployeeForAllPeriodsNotDeleted(employeeId, now))
                .map(mapper::fromEntry);
    }


    //<editor-fold desc="Implementation">
    @Transactional
    public Mono<Integer> reject(AuthContext auth, int salaryRequestId, SalaryRequestRejectBody body) {
        log.info("Reject salary request {} by {}", salaryRequestId, auth.getUsername());
        var now = dateTimeService.now();
        return validateFetchAndCheckPeriod(auth, salaryRequestId)
                .flatMap(entry -> {
                    if (entry.getImplementedBy() != null) {
                        return Mono.error(new BusinessError("errors.salary_request.already_implemented", Integer.toString(salaryRequestId)));
                    }
                    mapper.applyRequestRejectBody(entry, body, now, auth.getEmployeeInfo().getEmployeeId());
                    return requestRepo.save(entry)
                            .flatMap(rejectedEntry -> rescheduleIfRequired(auth, rejectedEntry, body.getRescheduleToNewPeriod()))
                            .flatMap(p -> historyDomainService.persistHistory(salaryRequestId,
                                    HistoryDomainService.HistoryEntityType.SALARY_REQUEST,
                                    p, now, auth.getEmployeeInfo().getEmployeeId()))
                            .map(HistoryEntry::getEntityId);
                });
    }


    /**
     * @param rejectedEntry
     * @param newPeriod
     * @return unmodified rejectedEntry
     */
    private Mono<SalaryRequestEntry> rescheduleIfRequired(AuthContext auth, SalaryRequestEntry rejectedEntry, Integer newPeriod) {
        if (newPeriod == null) {
            return Mono.just(rejectedEntry);
        }
        return closedPeriodCheck(newPeriod)
                .flatMap(period -> {
                    log.info("Reschedule salary request {} to period {} by {}", rejectedEntry.getId(), newPeriod, auth.getUsername());
                    var newRequestBody = SalaryRequestReportBody.builder()
                            .employeeId(rejectedEntry.getEmployeeId())
                            .type(rejectedEntry.getType())
                            .budgetBusinessAccount(rejectedEntry.getBudgetBusinessAccount())
                            .budgetExpectedFundingUntil(rejectedEntry.getBudgetExpectedFundingUntil())
                            .increaseAmount(rejectedEntry.getReqIncreaseAmount())
                            .currentSalaryAmount(rejectedEntry.getInfoCurrentSalaryAmount())
                            .previousSalaryIncreaseText(rejectedEntry.getInfoPreviousSalaryIncreaseText())
                            .previousSalaryIncreaseDate(rejectedEntry.getInfoPreviousSalaryIncreaseDate())
                            .plannedSalaryAmount(rejectedEntry.getReqPlannedSalaryAmount())
                            .increaseStartPeriod(newPeriod)
                            .reason(rejectedEntry.getReqReason())
                            .assessmentId(rejectedEntry.getAssessmentId())
                            .comment(rejectedEntry.getReqComment())
                            .build();

                    return domainService.doReport(auth, newRequestBody)
                            .flatMap(newRequestId -> {
                                var link = new SalaryRequestLinkCreateBody(
                                        rejectedEntry.getId(),
                                        newRequestId,
                                        SalaryRequestLinkType.RESCHEDULED.getId(),
                                        null);
                                return domainService.createLink(auth, link);
                            });
                }).map(newRequestId -> rejectedEntry);
    }


    @Transactional
    public Mono<Integer> markAsImplemented(AuthContext auth, int salaryRequestId, SalaryRequestImplementBody body) {
        log.info("Salary request {} marked to in progress by {}", salaryRequestId, auth.getUsername());
        var now = dateTimeService.now();
        return validateFetchAndCheckPeriod(auth, salaryRequestId)
                .flatMap(entry -> closedPeriodCheck(entry.getReqIncreaseStartPeriod())
                        .flatMap(c -> {
                            if (entry.getImplementedBy() != null) {
                                return Mono.error(new BusinessError("errors.salary_request.already_implemented", Integer.toString(salaryRequestId)));
                            }
                            mapper.applyRequestImplementBody(entry, body, now, auth.getEmployeeInfo().getEmployeeId());
                            return requestRepo.save(entry)
                                    .flatMap(p -> historyDomainService.persistHistory(salaryRequestId,
                                            HistoryDomainService.HistoryEntityType.SALARY_REQUEST,
                                            p, now, auth.getEmployeeInfo().getEmployeeId()))
                                    .map(HistoryEntry::getEntityId);
                        }));
    }

    @Transactional
    public Mono<Integer> updateImplIncreaseTextBody(AuthContext auth, int salaryRequestId, SalaryRequestUpdateImplIncreaseTextBody body) {
        log.info("Update implementation increase text for {} by {}", salaryRequestId, auth.getUsername());
        var now = dateTimeService.now();
        return validateFetchAndCheckPeriod(auth, salaryRequestId)
                .flatMap(entry -> {
                    if (entry.getImplementedBy() == null) {
                        return Mono.error(new BusinessError("errors.salary_request.not_implemented", Integer.toString(salaryRequestId)));
                    }
                    mapper.applyUpdateImplIncreaseTextBody(entry, body, now, auth.getEmployeeInfo().getEmployeeId());
                    return requestRepo.save(entry)
                            .flatMap(p -> historyDomainService.persistHistory(salaryRequestId,
                                    HistoryDomainService.HistoryEntityType.SALARY_REQUEST,
                                    p, now, auth.getEmployeeInfo().getEmployeeId()))
                            .map(HistoryEntry::getEntityId);
                });
    }

    @Transactional
    public Mono<Integer> resetImplementation(AuthContext auth, int salaryRequestId) {
        log.info("Reset implementation information for salary request {} by {}", salaryRequestId, auth.getUsername());
        var now = dateTimeService.now();
        return validateFetchAndCheckPeriod(auth, salaryRequestId)
                .flatMap(entry -> closedPeriodCheck(entry.getReqIncreaseStartPeriod())
                        .flatMap(c -> {
                            entry.setImplementedBy(null);
                            entry.setImplComment(null);
                            entry.setImplIncreaseText(null);
                            entry.setImplIncreaseAmount(null);
                            entry.setImplIncreaseStartPeriod(null);
                            entry.setImplNewPosition(null);
                            entry.setImplementedAt(null);
                            entry.setImplRejectReason(null);
                            entry.setImplSalaryAmount(null);
                            entry.setImplState(null);
                            return requestRepo.save(entry)
                                    .flatMap(p -> historyDomainService.persistHistory(salaryRequestId,
                                            HistoryDomainService.HistoryEntityType.SALARY_REQUEST,
                                            p, now, auth.getEmployeeInfo().getEmployeeId()))
                                    .map(HistoryEntry::getEntityId);
                        }));
    }

    private Mono<SalaryRequestEntry> validateFetchAndCheckPeriod(AuthContext auth, int salaryRequestId) {
        return secValidator.validateAdminSalaryRequest(auth)
                .flatMap(v -> requestRepo.findById(salaryRequestId))
                .switchIfEmpty(BusinessErrorFactory.entityNotFound(salaryRequestId))
                .flatMap(entry -> closedPeriodCheck(entry.getReqIncreaseStartPeriod()).thenReturn(entry)); // Проверка закрытого периода
    }

    //</editor-fold>

    //<editor-fold desc="Periods">
    @Transactional
    public Mono<Integer> closeSalaryRequestPeriod(AuthContext auth, @NonNull int periodId, @Nullable String comment) {
        log.info("Close salary request period {} by {}. Comment: {}", periodId, auth.getUsername(), comment);
        final var now = dateTimeService.now();
        return secValidator.validateCloseSalaryRequestPeriod(auth).flatMap(h -> {
                    var closedPeriod = new SalaryRequestClosedPeriodEntry();
                    closedPeriod.setPeriod(periodId);
                    closedPeriod.setClosedAt(now);
                    closedPeriod.setClosedBy(auth.getEmployeeInfo().getEmployeeId());
                    closedPeriod.setComment(comment);
                    return closedPeriodRepo.save(closedPeriod);
                })
                .flatMap(persisted ->
                        historyDomainService.persistHistory(periodId,
                                HistoryDomainService.HistoryEntityType.SALARY_REQUEST_CLOSED_REPORT_PERIOD,
                                persisted, now, auth.getEmployeeInfo().getEmployeeId())
                )
                .map(HistoryEntry::getEntityId);
    }

    @Transactional
    public Mono<Void> reopenSalaryRequestPeriod(AuthContext auth, @NonNull int periodId, @Nullable String comment) {
        log.info("Reopen salary request period {} by {}. Comment: {}", periodId, auth.getUsername(), comment);
        final var now = dateTimeService.now();
        return secValidator.validateCloseSalaryRequestPeriod(auth).
                flatMap(v ->
                        historyDomainService.persistHistory(periodId,
                                HistoryDomainService.HistoryEntityType.SALARY_REQUEST_CLOSED_REPORT_PERIOD,
                                null, now, auth.getEmployeeInfo().getEmployeeId())
                ).flatMap(h -> closedPeriodRepo.deleteById(periodId));
    }

    private Mono<Boolean> closedPeriodCheck(int periodId) {
        return closedPeriodRepo.findById(periodId)
                .flatMap(p -> Mono.error(new BusinessError("errors.salary_request.period_closed", Integer.toString(p.getPeriod()))))
                .map(e -> true)
                .defaultIfEmpty(true);
    }
    //</editor-fold>


}
