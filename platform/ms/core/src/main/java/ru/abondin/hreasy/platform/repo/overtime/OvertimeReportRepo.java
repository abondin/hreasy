package ru.abondin.hreasy.platform.repo.overtime;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface OvertimeReportRepo extends R2dbcRepository<OvertimeReportEntry, Integer> {
    @Query("select * from ovt.overtime_report where" +
            " employee=:employeeId" +
            " and period=:period")
    Mono<OvertimeReportEntry> get(@Param("employeeId") int employeeId,
                                  @Param("period") int period);


    @Query("-- All information from report\n" +
            "select r.*,\n" +
            "-- Summary of not deleted overtime items\n" +
            " (select sum(i.hours) from ovt.overtime_item i where i.report_id=r.id and i.deleted_at is null) as total_hours,\n" +
            "-- Last update of items\n" +
            " (select max(i.created_at) from ovt.overtime_item i where i.report_id=r.id and i.deleted_at is null) as last_update,\n" +
            "-- Last positive approval decision \n" +
            " (select max(a.decision_time) from ovt.overtime_approval_decision a where a.report_id=r.id \n" +
            "   and a.cancel_decision_time is null and a.decision='APPROVED') as last_approve,\n" +
            "-- Last negative approval decision\n" +
            " (select max(a.decision_time) from ovt.overtime_approval_decision a where a.report_id=r.id \n" +
            "   and a.cancel_decision_time is null and a.decision='DECLINED') as last_decline\n" +
            " from ovt.overtime_report r where r.period = :period")
    Flux<OvertimeReportEntry.OvertimeReportSummaryEntry> summary(@Param("period") int period);

}
