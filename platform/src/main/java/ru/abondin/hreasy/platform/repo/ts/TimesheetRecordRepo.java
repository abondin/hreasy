package ru.abondin.hreasy.platform.repo.ts;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Repository
public interface TimesheetRecordRepo extends ReactiveCrudRepository<TimesheetRecordEntry, Integer> {
    @Query("""
            select ts.*, e.displayName from ts.timesheet_record ts join empl.employee e on ts.employee=e.id
             where ts.date>=:from and ts.date<=to;
           """)
    Flux<TimesheetSummaryView> summary(LocalDate from, LocalDate to);
}
