package ru.abondin.hreasy.platform.service.salary;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.history.HistoryEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestClosedPeriodEntry;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestClosedPeriodRepo;
import ru.abondin.hreasy.platform.repo.salary.SalaryRequestRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestClosedPeriodDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestDto;
import ru.abondin.hreasy.platform.service.salary.dto.SalaryRequestMapper;

import javax.validation.constraints.NotNull;

@Service
@RequiredArgsConstructor
@Slf4j
public class SalaryRequestAdminService {
    private final SalaryRequestRepo requestRepo;

    private final SalaryRequestClosedPeriodRepo closedPeriodRepo;
    private final SalarySecurityValidator secValidator;
    private final SalaryRequestMapper mapper;

    private final HistoryDomainService historyDomainService;

    private final DateTimeService dateTimeService;

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
                }).flatMap(persisted ->
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

    public Flux<SalaryRequestDto> findAll(AuthContext auth, int periodId) {
        log.info("Getting all salary requests for {} by {}", periodId, auth);
        return secValidator.validateViewAllSalaryRequests(auth)
                .flatMapMany(v -> requestRepo.findAllNotDeleted(periodId, dateTimeService.now()))
                .map(mapper::fromEntry);
    }



}
