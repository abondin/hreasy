package ru.abondin.hreasy.platform.service.overtime;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeClosedPeriodEntry;
import ru.abondin.hreasy.platform.repo.overtime.OvertimeClosedPeriodRepo;
import ru.abondin.hreasy.platform.repo.overtime.OvertimePeriodHistoryEntry;
import ru.abondin.hreasy.platform.repo.overtime.OvertimePeriodHistoryRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;

import javax.validation.constraints.NotNull;

import static ru.abondin.hreasy.platform.repo.overtime.OvertimePeriodHistoryEntry.ACTION_CLOSE;
import static ru.abondin.hreasy.platform.repo.overtime.OvertimePeriodHistoryEntry.ACTION_REOPEN;


@Service
@RequiredArgsConstructor
@Slf4j
public class OvertimeAdminService {
    private final OvertimeClosedPeriodRepo closedPeriodRepo;
    private final OvertimePeriodHistoryRepo historyRepo;
    private final DateTimeService dateTimeService;

    private final OvertimeSecurityValidator securityValidator;


    @Transactional
    public Mono<Integer> closeOvertimePeriod(AuthContext auth, @NotNull int periodId, @Nullable String comment) {
        log.info("Close overtime period {} by {}. Comment: {}", periodId, auth.getUsername(), comment);
        final var now = dateTimeService.now();
        return securityValidator.validateAdminOvertime(auth).flatMap((v) -> {
            var history = new OvertimePeriodHistoryEntry();
            history.setPeriod(periodId);
            history.setComment(comment);
            history.setAction(ACTION_CLOSE);
            history.setUpdatedBy(auth.getEmployeeInfo().getEmployeeId());
            history.setUpdatedAt(now);
            return historyRepo.save(history);
        }).flatMap(h -> {
            var closedPeriod = new OvertimeClosedPeriodEntry();
            closedPeriod.setPeriod(periodId);
            closedPeriod.setClosedAt(now);
            closedPeriod.setClosedBy(auth.getEmployeeInfo().getEmployeeId());
            closedPeriod.setComment(comment);
            return closedPeriodRepo.save(closedPeriod);
        }).map(OvertimeClosedPeriodEntry::getPeriod);
    }

    @Transactional
    public Mono<Void> reopenOvertimePeriod(AuthContext auth, @NotNull int periodId, @Nullable String comment) {
        log.info("Reopen overtime period {} by {}. Comment: {}", periodId, auth.getUsername(), comment);
        final var now = dateTimeService.now();
        return securityValidator.validateAdminOvertime(auth).flatMap((v) -> {
            var history = new OvertimePeriodHistoryEntry();
            history.setPeriod(periodId);
            history.setComment(comment);
            history.setAction(ACTION_REOPEN);
            history.setUpdatedBy(auth.getEmployeeInfo().getEmployeeId());
            history.setUpdatedAt(now);
            return historyRepo.save(history);
        }).flatMap(h -> closedPeriodRepo.deleteById(periodId));
    }

}
