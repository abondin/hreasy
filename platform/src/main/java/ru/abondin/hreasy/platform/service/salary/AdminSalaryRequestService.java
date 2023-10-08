package ru.abondin.hreasy.platform.service.salary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.history.HistoryEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestClosedPeriodEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestClosedPeriodRepo;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestImplementBody;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestMapper;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestRejectBody;

import javax.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class AdminSalaryRequestService {
    private final SalaryRequestRepo requestRepo;

    private final SalaryRequestClosedPeriodRepo closedPeriodRepo;
    private final SalarySecurityValidator secValidator;
    private final SalaryRequestMapper mapper;

    private final HistoryDomainService historyDomainService;

    private final DateTimeService dateTimeService;

    public Flux<SalaryRequestDto> findAll(AuthContext auth, int periodId) {
        var now = dateTimeService.now();
        log.debug("Get all requests for period {} by {}", periodId, auth);
        return secValidator.validateViewAll(auth)
                .flatMapMany(v -> requestRepo.findAllNotDeleted(periodId, now))
                .map(mapper::fromEntry);
    }

    @Transactional
    public Mono<Integer> reject(AuthContext auth, int salaryRequestId, SalaryRequestRejectBody body) {
        log.info("Reject salary request {} by {}", salaryRequestId, auth.getUsername());
        var now = dateTimeService.now();
        return secValidator.validateAdminSalaryRequest(auth).flatMap(v ->
                        requestRepo.findById(salaryRequestId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(salaryRequestId))))
                .flatMap(entry -> {
                    if (entry.getImplementedBy() != null) {
                        return Mono.error(new BusinessError("errors.salary_request.already_implemented", Integer.toString(salaryRequestId)));
                    }
                    mapper.applyRequestRejectBody(entry, body, now, auth.getEmployeeInfo().getEmployeeId());
                    return requestRepo.save(entry).map(SalaryRequestEntry::getId);
                });
    }

    @Transactional
    public Mono<Integer> markAsImplemented(AuthContext auth, int salaryRequestId, SalaryRequestImplementBody body) {
        log.info("Salary request {} marked to in progress by {}", salaryRequestId, auth.getUsername());
        var now = dateTimeService.now();
        return secValidator.validateAdminSalaryRequest(auth).flatMap(v ->
                        requestRepo.findById(salaryRequestId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", Integer.toString(salaryRequestId))))
                .flatMap(entry -> {
                    if (entry.getImplementedBy() != null) {
                        return Mono.error(new BusinessError("errors.salary_request.already_implemented", Integer.toString(salaryRequestId)));
                    }
                    mapper.applyRequestImplementBody(entry, body, now, auth.getEmployeeInfo().getEmployeeId());
                    return requestRepo.save(entry).map(SalaryRequestEntry::getId);
                });
    }

    @Transactional
    public Mono<Integer> closeSalaryRequestPeriod(AuthContext auth, @NotNull int periodId, @Nullable String comment) {
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
    public Mono<Void> reopenSalaryRequestPeriod(AuthContext auth, @NotNull int periodId, @Nullable String comment) {
        log.info("Reopen salary request period {} by {}. Comment: {}", periodId, auth.getUsername(), comment);
        final var now = dateTimeService.now();
        return secValidator.validateCloseSalaryRequestPeriod(auth).
                flatMap(v ->
                        historyDomainService.persistHistory(periodId,
                                HistoryDomainService.HistoryEntityType.SALARY_REQUEST_CLOSED_REPORT_PERIOD,
                                null, now, auth.getEmployeeInfo().getEmployeeId())
                ).flatMap(h -> closedPeriodRepo.deleteById(periodId));
    }

}
