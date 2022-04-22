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
            " e.lastname employee_lastname, e.firstname employee_firstname, e.patronymic_name employee_patronymic_name" +
            ", e.date_of_employment employee_date_of_employment,  a.*, p.id employee_current_project_id, p.name employee_current_project_name  " +
            " from empl.employee e left join assmnt.assessment a on e.id=a.employee" +
            " left join proj.project p on e.current_project=p.id " +
            " where (e.date_of_dismissal is null or e.date_of_dismissal > :now)" +
            " and (a.canceled_at is null or a.canceled_at > :now)" +
            " order by a.created_at desc, e.date_of_employment desc")
    Flux<EmployeeAssessmentEntry> findNotCanceledAssessmentForNotFired(@Param("now") OffsetDateTime now);

    @Query("select a.*, " +
            " empl.lastname employee_lastname, empl.firstname employee_firstname, empl.patronymic_name employee_patronymic_name" +
            " ,createdBy.lastname created_by_lastname, createdBy.firstname created_by_firstname, createdBy.patronymic_name created_by_patronymic_name" +
            " ,completedBy.lastname completed_by_lastname, completedBy.firstname completed_by_firstname, completedBy.patronymic_name completed_by_patronymic_name" +
            " ,canceledBy.lastname canceled_by_lastname, canceledBy.firstname canceled_by_firstname, canceledBy.patronymic_name canceled_by_patronymic_name" +
            " from assmnt.assessment a" +
            " left join empl.employee empl on a.employee=empl.id " +
            " left join empl.employee createdBy on a.created_by=createdBy.id " +
            " left join empl.employee completedBy on a.completed_by=completedBy.id " +
            " left join empl.employee canceledBy on a.canceled_by=canceledBy.id " +
            " where a.employee=:employee" +
            " order by a.created_at desc")
    Flux<AssessmentViewEntry> findByEmployeeId(int employee);

    @Query("select a.*, " +
            " empl.lastname employee_lastname, empl.firstname employee_firstname, empl.patronymic_name employee_patronymic_name" +
            " ,createdBy.lastname created_by_lastname, createdBy.firstname created_by_firstname, createdBy.patronymic_name created_by_patronymic_name" +
            " ,completedBy.lastname completed_by_lastname, completedBy.firstname completed_by_firstname, completedBy.patronymic_name completed_by_patronymic_name" +
            " ,canceledBy.lastname canceled_by_lastname, canceledBy.firstname canceled_by_firstname, canceledBy.patronymic_name canceled_by_patronymic_name" +
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
}
