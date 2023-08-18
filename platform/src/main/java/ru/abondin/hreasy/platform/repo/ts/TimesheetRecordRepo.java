package ru.abondin.hreasy.platform.repo.ts;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.LocalDate;
import java.time.OffsetDateTime;

@Repository
public interface TimesheetRecordRepo extends ReactiveCrudRepository<TimesheetRecordEntry, Integer> {

    /**
     * @param from
     * @param to
     * @param ba
     * @param project
     * @return all not dismissed employees.
     * For every employee returns all timesheet for given filter and all vacations for given filter's dates
     */
    @Query("""
            select e.id employee_id,
                e.display_name employee_display_name,
                e.current_project employee_current_project,
                p.ba_id employee_ba,
                jsonb_agg(distinct
                    (case when ts.id is null then null
                     else jsonb_build_object('id', ts.id, 'hoursSpent', ts.hours_spent,
                          'date', ts.date, 'comment', ts.comment, 'employee', ts.employee,
                          'project', ts.project, 'businessAccount', ts.business_account)
                     end)
                )timesheet,
                jsonb_agg(
                    distinct (case when v.id is null then null else jsonb_build_object('id', v.id, 'start_date',v.start_date, 'end_date', v.end_date) end)
                ) vacations
            from empl.employee e
            left join proj.project p on e.current_project = p.id
            left join
                (select * from ts.timesheet_record
                    where "date" between :from and :to and (:ba is null or business_account = :ba) and (:project is null or project = :project)) ts
             on ts.employee = e.id
            left join
                (select * from vac.vacation v where v.start_date <= :to and v.end_date >= :from and v.stat in (0,1,2)) v
                on v.employee = e.id
            where (e.date_of_dismissal  is null or e.date_of_dismissal >= :now)
            group by e.id,e.display_name,e.current_project,p.ba_id
            order by e.id
            """)
    Flux<TimesheetSummaryView> summary(@Param("fr") LocalDate from, @Param("t") LocalDate to,
                                       @Param("ba") Integer ba, @Param("project") Integer project, OffsetDateTime now);

    @Query("select ts.* from ts.timesheet_record ts " +
            " where ts.employee=:employeeId" +
            " and ts.business_account=:businessAccount" +
            " and ts.project=:project" +
            " and ts.date=:date"
    )
    Mono<TimesheetRecordEntry> find(int employeeId, int businessAccount, Integer project, LocalDate date);
}
