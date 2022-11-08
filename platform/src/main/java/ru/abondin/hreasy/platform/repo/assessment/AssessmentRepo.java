package ru.abondin.hreasy.platform.repo.assessment;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Repository
public interface AssessmentRepo extends ReactiveCrudRepository<AssessmentEntry, Integer> {

    @Query("select e.id employee_id," +
            " e.display_name employee_displayname " +
            ", e.date_of_employment employee_date_of_employment,  a.*" +
            ", p.id employee_current_project_id, p.name employee_current_project_name, e.current_project_role employee_current_project_role" +
            ", ba.id ba_id, ba.name ba_name" +
            " from empl.employee e left join assmnt.assessment a on e.id=a.employee" +
            " left join proj.project p on e.current_project=p.id " +
            " left join ba.business_account ba on p.ba_id=ba.id " +
            " where (e.date_of_dismissal is null or e.date_of_dismissal > :now)" +
            " and (a.canceled_at is null or a.canceled_at > :now)" +
            " order by a.created_at desc, e.date_of_employment desc")
    Flux<EmployeeAssessmentEntry> findNotCanceledAssessmentForNotFired(@Param("now") OffsetDateTime now);

    @Query("select a.*, " +
            " empl.display_name employee_displayname" +
            " ,createdBy.display_name created_by_displayname" +
            " ,completedBy.display_name completed_by_displayname" +
            " ,canceledBy.display_name canceled_by_displayname" +
            " from assmnt.assessment a" +
            " left join empl.employee empl on a.employee=empl.id " +
            " left join empl.employee createdBy on a.created_by=createdBy.id " +
            " left join empl.employee completedBy on a.completed_by=completedBy.id " +
            " left join empl.employee canceledBy on a.canceled_by=canceledBy.id " +
            " where a.employee=:employee" +
            " order by a.created_at desc")
    Flux<AssessmentViewEntry> findByEmployeeId(int employee);

    @Query("select a.*, " +
            " empl.display_name employee_displayname" +
            " ,createdBy.display_name created_by_displayname" +
            " ,completedBy.display_name completed_by_displayname" +
            " ,canceledBy.display_name canceled_by_displayname" +
            " from assmnt.assessment a" +
            " left join empl.employee empl on a.employee=empl.id " +
            " left join empl.employee createdBy on a.created_by=createdBy.id " +
            " left join empl.employee completedBy on a.completed_by=completedBy.id " +
            " left join empl.employee canceledBy on a.canceled_by=canceledBy.id " +
            " where a.id=:assessmentId")
    Mono<AssessmentViewEntry> findById(int assessmentId);

    /**
     * Find assessments initiator with all forms owners
     *
     * @param assessmentId
     * @return
     */
    @Query("select DISTINCT owner from (\n" +
            "select a.created_by owner from assmnt.assessment a where a.id=:assessmentId\n" +
            "union\n" +
            "select owner owner from assmnt.assessment_form f where f.assessment_id =:assessmentId\n" +
            ") owners where owner is not null")
    Flux<Integer> findAllAssessmentOwners(@Param("assessmentId") int assessmentId);

    @Query("update assmnt.assessment set canceled_by=:canceledBy, canceled_at=:canceledAt" +
            " where id=:assessmentId")
    Mono<? extends Integer> updateCanceledBy(int assessmentId, Integer canceledBy, OffsetDateTime canceledAt);

    @Query("update assmnt.assessment set completed_by=:canceledBy, completed_at=:canceledAt" +
            " where id=:assessmentId")
    Mono<? extends Integer> updateCompletedBy(int assessmentId, Integer canceledBy, OffsetDateTime canceledAt);
}
