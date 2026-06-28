package ru.abondin.hreasy.platform.repo.employee.projecttransfer;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Repository
public interface ProjectTransferRequestRepo extends ReactiveCrudRepository<ProjectTransferRequestEntry, Integer> {

    @Query("""
            select *
            from empl.project_transfer_request
            where employee_id = :employeeId
              and state = 1
            """)
    Mono<ProjectTransferRequestEntry> findPendingByEmployeeId(int employeeId);

    @Query("""
            select *
            from empl.project_transfer_request
            where id = :id
              and state = 1
            """)
    Mono<ProjectTransferRequestEntry> findPendingById(int id);

    @Query("""
            select request.*,
                   from_project.name as from_project_name,
                   to_project.name as to_project_name,
                   created_by.display_name as created_by_display_name,
                   approver.display_name as approver_display_name
            from empl.project_transfer_request request
                join proj.project from_project on request.from_project_id = from_project.id
                join proj.project to_project on request.to_project_id = to_project.id
                join empl.employee created_by on request.created_by = created_by.id
                join empl.employee approver on request.approver_employee_id = approver.id
            where request.employee_id = :employeeId
              and request.state = 1
            """)
    Mono<ProjectTransferRequestView> findPendingViewByEmployeeId(int employeeId);

    @Query("""
            select request.*,
                   from_project.name as from_project_name,
                   to_project.name as to_project_name,
                   created_by.display_name as created_by_display_name,
                   approver.display_name as approver_display_name
            from empl.project_transfer_request request
                join proj.project from_project on request.from_project_id = from_project.id
                join proj.project to_project on request.to_project_id = to_project.id
                join empl.employee created_by on request.created_by = created_by.id
                join empl.employee approver on request.approver_employee_id = approver.id
            where request.id = :id
              and request.state = 1
            """)
    Mono<ProjectTransferRequestView> findPendingViewById(int id);

    @Query("""
            update empl.project_transfer_request
            set state = 5,
                updated_at = :now,
                updated_by = null
            where state = 1
              and created_at <= :createdBefore
            returning *
            """)
    Flux<ProjectTransferRequestEntry> expirePendingCreatedBefore(OffsetDateTime createdBefore, OffsetDateTime now);
}
