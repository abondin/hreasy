package ru.abondin.hreasy.platform.repo.ts;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;

@Repository
public interface TimesheetRecordRepo extends ReactiveCrudRepository<TimesheetRecordEntry, Integer> {
    @Query("select ts.* from ts.timesheet_record ts " +
            " where ts.date>=:fr and ts.date<=:t")
    Flux<TimesheetRecordEntry> summary(@Param("fr") LocalDate from, @Param("t") LocalDate to);

    @Query("select ts.* from ts.timesheet_record ts " +
            " where ts.employee=:employeeId" +
            " and ts.business_account=:businessAccount" +
            " and ts.project=:project" +
            " and ts.date=:date"
    )
    Mono<TimesheetRecordEntry> find(int employeeId, int businessAccount, Integer project, LocalDate date);
}
