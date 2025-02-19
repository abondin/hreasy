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

}

