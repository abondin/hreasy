package ru.abondin.hreasy.platform.repo.overtime;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.r2dbc.repository.R2dbcRepository;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

/**
 * Approval of overtime report of employee
 */
@Repository
public interface OvertimeApprovalDecisionRepo extends R2dbcRepository<OvertimeApprovalDecisionEntry, Integer> {

    @Query("select d.*," +
            " e.firstname as approver_firstname, e.lastname as approver_lastname, e.patronymic_name as approver_patronymic_name" +
            " from ovt.overtime_approval_decision d" +
            " join empl.employee e on d.approver=e.id" +
            " where d.report_id=:reportId and d.cancel_decision_time is null order by d.decision_time")
    Flux<OvertimeApprovalDecisionEntry.OvertimeApprovalDecisionWithEmployeeEntry> findNotCanceledByReportId(@Param("reportId") int reportId);

    @Query("update ovt.overtime_approval_decision where id=:approvalId set cancel_decision_time=:cancelDecisionTime")
    int cancelDecision(@Param("approvalId") int reportId, @Param("cancelDecisionTime") OffsetDateTime cancelDecisionTime);

    @Query("select cast(case when count(id)>0 then true else false end as boolean) as canceled_exist\n" +
            " from ovt.overtime_approval_decision where\n" +
            " report_id=:reportId and approver=:approver and cancel_decision_time is null")
    Mono<Boolean> existsNotCanceled(@Param("reportId") Integer reportId, @Param("approver") Integer approver);
}
