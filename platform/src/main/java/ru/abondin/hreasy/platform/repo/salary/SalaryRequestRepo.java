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
            select * from sal.v_salary_request_full r 
            """;
    String GET_SALARY_REQUEST_VIEW_NOT_DELETED_SQL = GET_SALARY_REQUEST_VIEW_BASE_SQL + " where (r.deleted_at is null or r.deleted_at > :now) ";


    @Query(GET_SALARY_REQUEST_VIEW_NOT_DELETED_SQL + " and r.id = :id")
    Mono<SalaryRequestView> findFullNotDeletedById(Integer id, OffsetDateTime now);

    @Query(GET_SALARY_REQUEST_VIEW_NOT_DELETED_SQL + " and r.req_increase_start_period=:period and (" +
            "r.created_by =:createdBy " +
            "or r.budget_business_account in (:bas)" +
            ") order by employee_display_name")
    Flux<SalaryRequestView> findNotDeleted(int period, int createdBy, List<Integer> bas, OffsetDateTime now);


    @Query(GET_SALARY_REQUEST_VIEW_NOT_DELETED_SQL + " and r.req_increase_start_period=:period and (" +
            "r.created_by =:createdBy " +
            ") order by employee_display_name")
    Flux<SalaryRequestView> findNotDeletedMy(int period, int createdBy, OffsetDateTime now);


    @Query(GET_SALARY_REQUEST_VIEW_NOT_DELETED_SQL + " and r.req_increase_start_period=:periodId order by employee_display_name")
    Flux<SalaryRequestView> findAllNotDeleted(int periodId, OffsetDateTime now);

    @Query(GET_SALARY_REQUEST_VIEW_NOT_DELETED_SQL + " and r.employee_id=:employeeId order by r.req_increase_start_period desc")
    Flux<SalaryRequestView> findAllForEmployeeForAllPeriodsNotDeleted(int employeeId, OffsetDateTime now);

    @Query("""
                SELECT
                e.id AS employee_id,
                e.display_name AS employee_display_name,
                e.email AS employee_email,
                ba.id AS employee_business_account_id,
                ba.name AS employee_business_account_name,
                p.id AS employee_current_project_id,
                p.name AS employee_current_project_name,
                e.current_project_role AS employee_current_project_role,
                e.date_of_employment AS employee_date_of_employment,
                sr.id as request_id,
                sr.created_at AS request_created_at,
                sr.req_increase_start_period AS request_start_period,
                sr.req_increase_amount AS request_req_increase_amount,
                sr.impl_increase_amount AS request_impl_increase_amount,
                sr.impl_salary_amount AS request_impl_salary_amount,
                sr.impl_state AS request_impl_state
            FROM empl.employee e
            LEFT JOIN (
                SELECT DISTINCT ON (sr1.employee_id)
                    sr1.*
                FROM
                    sal.salary_request sr1
                WHERE
                    sr1."type" = 1
                    AND (sr1.deleted_at IS NULL OR sr1.deleted_at > :now)
                ORDER BY
                    sr1.employee_id, sr1.created_at DESC
            ) sr ON e.id = sr.employee_id
            LEFT JOIN proj.project p ON e.current_project = p.id
            LEFT JOIN ba.business_account ba ON p.ba_id = ba.id
            WHERE
                e.date_of_dismissal IS NULL OR e.date_of_dismissal > :now
            ORDER BY
                e.display_name            
            """)
    Flux<EmployeeWithLatestSalaryRequestView> findLatestIncreases(OffsetDateTime now);
}

