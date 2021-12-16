package ru.abondin.hreasy.platform.repo.overtime;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OvertimeItemViewRepo extends R2dbcRepository<OvertimeItemsGroupedByDateAndProjectView, Integer> {
    @Query("select date, project_id, report_id, sum(hours) as hours\n" +
            " from ovt.overtime_item where\n" +
            " deleted_at is null\n" +
            " and report_id = :reportId\n" +
            " group by date, project_id, report_id;")
    Flux<OvertimeItemsGroupedByDateAndProjectView> gropedByProjectAndDate(@Param("reportId") int reportId);

}
