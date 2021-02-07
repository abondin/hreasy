package ru.abondin.hreasy.platform.repo.overtime;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import reactor.core.publisher.Flux;

public interface OvertimeClosedPeriodRepo extends R2dbcRepository<OvertimeClosedPeriodEntry, Integer> {
    @Query("select * from overtime_closed_period")
    Flux<OvertimeClosedPeriodEntry> allClosed();
}
