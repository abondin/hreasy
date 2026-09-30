package ru.abondin.hreasy.platform.repo.overtime;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.LocalDate;

@Repository
public interface OvertimeItemViewRepo extends R2dbcRepository<OvertimeItemsGroupedByDateAndProjectView, Integer> {
    @Query("select date, project_id, report_id, sum(hours) as hours\n" +
            " from ovt.overtime_item where\n" +
            " deleted_at is null\n" +
            " and report_id = :reportId\n" +
            " group by date, project_id, report_id")
    Flux<OvertimeItemsGroupedByDateAndProjectView> gropedByProjectAndDate(@Param("reportId") int reportId);

    @Query("""
            select i.date, i.project_id, i.report_id, i.workstream_id,
                   w.external_id as workstream_external_id, w.display_name as workstream_display_name,
                   sum(i.hours) as hours
            from ovt.overtime_item i
            left join proj.project_workstream w on w.id = i.workstream_id
            where i.deleted_at is null and i.report_id = :reportId
            group by i.date, i.project_id, i.report_id, i.workstream_id, w.external_id, w.display_name
            order by i.date, i.project_id, i.workstream_id
            """)
    Flux<OvertimeWorkstreamSummaryView> groupedByWorkstream(@Param("reportId") int reportId);

    record OvertimeWorkstreamSummaryView(LocalDate date, int projectId, int reportId, Integer workstreamId,
                                        String workstreamExternalId, String workstreamDisplayName, float hours) {
    }

}
