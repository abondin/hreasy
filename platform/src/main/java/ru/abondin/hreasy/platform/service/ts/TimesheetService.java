package ru.abondin.hreasy.platform.service.ts;


import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.auth.AuthContext;
import ru.abondin.hreasy.platform.repo.ts.TimesheetRecordRepo;
import ru.abondin.hreasy.platform.service.DateTimeService;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetAggregatedFilter;
import ru.abondin.hreasy.platform.service.ts.dto.TimesheetSummaryDto;

@Service
@Slf4j
@RequiredArgsConstructor
public class TimesheetService {
    private final DateTimeService dateTimeService;
    private final TimesheetRecordRepo repo;

    private final TimesheetSecurityValidator sec;

    public Flux<TimesheetSummaryDto> timesheetSummary(AuthContext ctx, TimesheetAggregatedFilter filter) {
        log.info("Get timesheet summary by {}: {}", ctx.getUsername(), filter);
        return sec.validateViewTimesheetSummary(ctx)
                .flatMapMany(v -> repo.summary(filter.getFrom(), filter.getTo()))
                .map();
    }
}
