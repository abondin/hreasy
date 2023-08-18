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
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetMapper;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetQueryFilter;
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
    public Flux<TimesheetSummaryDto> timesheetSummary(AuthContext ctx, TimesheetQueryFilter filter) {
        log.info("Get timesheet summary by {}: {}", ctx.getUsername(), filter);
        return sec.validateViewTimesheetSummary(ctx)
                .flatMapMany(v -> repo.summary(filter.getFrom(), filter.getTo(), filter.getBa(), filter.getProject(), dateTimeService.now()))
                .map(v -> mapper.fromView(v, filter.getBa(), filter.getProject()));
    }

    @Transactional
    public Flux<Integer> report(AuthContext auth, int employeeId, TimesheetReportBody body) {
        log.info("Report timesheet by {}: {}", auth.getUsername(), body);
        var createdAt = dateTimeService.now();
        var createdBy = auth.getEmployeeInfo().getEmployeeId();
        // 1. Validate access
        return sec.validateReportTimesheet(auth, employeeId)
                // 2. Map every day in report body to list of separate database entries
                .flatMapMany(v -> Flux.fromIterable(body.getHours())
                        // 3. Check if we have timesheet record in database
                        .flatMap(h ->
                                repo.find(employeeId, body.getBusinessAccount(), body.getProject(), h.date())
                                        .defaultIfEmpty(mapper.toBaseEntry(employeeId,
                                                body.getBusinessAccount(),
                                                body.getProject()))
                                        // 4. Update fields
                                        .map(baseEntry -> mapper.applyChanges(baseEntry, h, body.getComment(),
                                                createdAt, createdBy)))
                )
                // 3. Save every timesheet entry
                .flatMap(repo::save)
                // 4. Save every timesheet entry history
                .flatMap(persistent -> history.persistHistory(persistent.getId(),
                                HistoryDomainService.HistoryEntityType.TIMESHEET_RECORD, persistent,
                                createdAt, createdBy)
                        // 5. Return ids of created timesheet records
                        .map(h -> persistent.getId()));
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
