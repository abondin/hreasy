package ru.abondin.hreasy.platform.service.ts;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import ru.abondin.hreasy.platform.BusinessError;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.ts.TimesheetRecordEntry;
import ru.abondin.hreasy.platform.repo.ts.TimesheetRecordRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
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
    private final TimesheetSecurityValidator sec;

    @Transactional(readOnly = true)
    public Flux<TimesheetSummaryDto> timesheetSummary(AuthContext ctx, TimesheetAggregatedFilter filter) {
        log.info("Get timesheet summary by {}: {}", ctx.getUsername(), filter);
        return sec.validateViewTimesheetSummary(ctx)
                .flatMapMany(v -> repo.summary(filter.getFrom(), filter.getTo()))
                .map(mapper::fromEntry);
    }

    @Transactional
    public Mono<Integer> report(AuthContext ctx, int employeeId, TimesheetReportBody body) {
        log.info("Report timesheet by {}: {}", ctx.getUsername(), body);
        return sec.validateReportTimesheet(ctx, employeeId)
                .map(v -> mapper.toEntry(employeeId, body, dateTimeService.now(), ctx.getEmployeeInfo().getEmployeeId()))
                .flatMap(repo::save)
                .map(TimesheetRecordEntry::getId);
    }

    public Mono<Integer> delete(AuthContext ctx, Integer employeeId, Integer timesheetRecordId) {
        log.info("Delete timesheet record {} of employee {} by {}", timesheetRecordId, employeeId, ctx.getUsername());
        return sec.validateReportTimesheet(ctx, employeeId)
                .flatMap(v -> repo.findById(timesheetRecordId))
                .switchIfEmpty(Mono.error(new BusinessError("errors.entity.not.found", employeeId + ":" + timesheetRecordId)))
                .flatMap(entry -> {
                    entry.setDeletedBy(ctx.getEmployeeInfo().getEmployeeId());
                    entry.setDeletedAt(dateTimeService.now());
                    return repo.save(entry);
                }).map(TimesheetRecordEntry::getId);
    }
}
