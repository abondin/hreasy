package ru.abondin.hreasy.platform.repo.overtime;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface OvertimeItemViewRepo extends R2dbcRepository<OvertimeItemView, Integer> {
    @Query("select " +
            "i.id id, i.report_id report_id, r.employee_id report_employee_id, r.period report_period," +
            " i.hours hours, i.project_id project_id, i.date date from overtime_item i" +
            " left join overtime_report r on i.report_id=r.id" +
            " where r.period=:period" +
            " and i.deleted_at is null" +
            " order by r.employee_id, i.date")
    Flux<OvertimeItemView> findNotDeleted(@Param("period") int period);

}
