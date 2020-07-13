package ru.abondin.hreasy.platform.repo.overtime;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

public interface OvertimeReportRepo extends ReactiveCrudRepository<OvertimeReportEntry, Integer> {
    @Query("select * from overtime_report where" +
            " employee_id=:employeeId" +
            " and period=:period")
    Mono<OvertimeReportEntry> get(@Param("employeeId") int employeeId,
                                  @Param("period") int period);
}
