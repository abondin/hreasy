package ru.abondin.hreasy.platform.repo.salary;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.List;

@Repository
public interface SalaryRequestRepo extends ReactiveCrudRepository<SalaryRequestEntry, Integer> {

    String GET_SALARY_REQUEST_VIEW_BASE_SQL = """
            select 
                r.*,
                e.display_name as employee_display_name,
                p.name info_empl_project_name,
                pos.name as info_empl_position,
                ba.name as info_empl_ba,
                budget_ba.name as budget_business_account_name,
                asm.planned_date as assessment_planned_date,
                cr.display_name as created_by_display_name,
                im.display_name as implemented_by_display_name,
                newPos.name as impl_new_position_name
            from sal.salary_request r
                left join empl.employee e on r.employee_id = e.id
                left join proj.project p on e.current_project=p.id
                left join dict.position pos on e.position = pos.id 
                left join ba.business_account ba on r.budget_business_account=ba.id
                left join ba.business_account budget_ba on r.budget_business_account=budget_ba.id
                left join assmnt.assessment asm on r.assessment_id=asm.id
                left join empl.employee cr on r.created_by = cr.id
                left join empl.employee im on r.implemented_by = im.id
                left join dict.position newPos on r.impl_new_position = newPos.id 
            """;
    String GET_SALARY_REQUEST_VIEW_NOT_DELETED_SQL = GET_SALARY_REQUEST_VIEW_BASE_SQL + " where (r.deleted_at is null or r.deleted_at > :now) ";

    @Query(GET_SALARY_REQUEST_VIEW_NOT_DELETED_SQL + " and r.id = :id")
    Mono<SalaryRequestView> findFullNotDeletedById(Integer id, OffsetDateTime now);

    @Query(GET_SALARY_REQUEST_VIEW_NOT_DELETED_SQL + " and r.req_increase_start_period=:period and (" +
            "r.created_by =:createdBy " +
            "or r.budget_business_account in (:bas)" +
            ") order by r.created_at desc")
    Flux<SalaryRequestView> findNotDeleted(int period, int createdBy, List<Integer> bas, OffsetDateTime now);

    @Query(GET_SALARY_REQUEST_VIEW_NOT_DELETED_SQL + " and r.req_increase_start_period=:period and (" +
            "r.created_by =:createdBy " +
            ") order by r.created_at desc")
    Flux<SalaryRequestView> findNotDeletedMy(int period, int createdBy, OffsetDateTime now);


    @Query(GET_SALARY_REQUEST_VIEW_NOT_DELETED_SQL+" and r.req_increase_start_period=:periodId order by r.created_at desc")
    Flux<SalaryRequestView> findAllNotDeleted(int periodId, OffsetDateTime now);
}

