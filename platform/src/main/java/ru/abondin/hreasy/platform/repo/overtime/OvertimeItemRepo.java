package ru.abondin.hreasy.platform.repo.overtime;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface OvertimeItemRepo extends ReactiveCrudRepository<OvertimeItemEntry, Integer> {
    @Query("select * from overtime_item where" +
            " report_id=:report_id" +
            " order by date")
    Flux<OvertimeItemEntry> get(@Param("reportId") int reportId);
}
