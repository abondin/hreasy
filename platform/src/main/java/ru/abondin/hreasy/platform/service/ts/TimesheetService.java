package ru.abondin.hreasy.platform.service.ts;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.ts.TimesheetRecordRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.HistoryDomainService;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetAggregatedFilter;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetMapper;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetReportBody;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetSummaryDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimesheetService {
    private final DateTimeService dateTimeService;
    private final TimesheetRecordRepo repo;

    private final TimesheetMapper mapper;

    private final HistoryDomainService history;
    private final TimesheetSecurityValidator sec;

    @Transactional(readOnly = true)
    public Flux<TimesheetSummaryDto> timesheetSummary(AuthContext ctx, TimesheetAggregatedFilter filter) {
        log.info("Get timesheet summary by {}: {}", ctx.getUsername(), filter);
        return sec.validateViewTimesheetSummary(ctx)
                .flatMapMany(v -> repo.summary(filter.getFrom(), filter.getTo()))
                .map(mapper::fromEntry);
    }

    @Transactional
    public Mono<Integer> report(AuthContext auth, int employeeId, TimesheetReportBody body) {
        log.info("Report timesheet by {}: {}", auth.getUsername(), body);
        var createdAt = dateTimeService.now();
        var createdBy = auth.getEmployeeInfo().getEmployeeId();
        return sec.validateReportTimesheet(auth, employeeId)
                .map(v -> mapper.toEntry(employeeId, body, createdAt, createdBy))
                .flatMap(repo::save)
                .flatMap(persistent -> history.persistHistory(persistent.getId(),
                        HistoryDomainService.HistoryEntityType.TIMESHEET_RECORD, persistent,
                        createdAt, createdBy).map(h -> persistent.getId()));
    }

    @Transactional
    public Mono<Void> delete(AuthContext auth, Integer employeeId, Integer timesheetRecordId) {
        log.info("Delete timesheet record {} of employee {} by {}", timesheetRecordId, employeeId, auth.getUsername());
        var deletedAt = dateTimeService.now();
        var deletedBy = auth.getEmployeeInfo().getEmployeeId();
        return sec.validateUpdateOrDeleteTimesheet(auth, employeeId, timesheetRecordId)
                .flatMap(v ->
                        history.persistHistory(
                                timesheetRecordId, HistoryDomainService.HistoryEntityType.TIMESHEET_RECORD, null, deletedAt, deletedBy))
                .flatMap(v -> repo.deleteById(timesheetRecordId));
    }
}
