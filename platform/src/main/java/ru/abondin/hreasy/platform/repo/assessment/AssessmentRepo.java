package ru.abondin.hreasy.platform.repo.assessment;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

@Repository
public interface AssessmentRepo extends ReactiveCrudRepository<AssessmentEntry, Integer> {

    @Query("select e.id employee_id," +
            " e.lastname employee_lastname, e.firstname employee_firstname, e.patronymic_name employee_patronymic_name" +
            ", e.date_of_employment employee_date_of_employment,  a.*, p.id employee_current_project_id, p.name employee_current_project_name  " +
            " from employee e left join assessment a on e.id=a.employee" +
            " left join project p on e.current_project=p.id " +
            " where e.date_of_dismissal is null or e.date_of_dismissal > :now" +
            " and a.canceled_at is null or a.canceled_at > :now" +
            " order by a.created_at desc, e.date_of_employment desc")
    Flux<EmployeeAssessmentEntry> findNotCanceledAssessmentForNotFired(@Param("now") OffsetDateTime now);

    @Query("select a.*, " +
            " createdBy.lastname created_by_lastname, createdBy.firstname created_by_firstname, createdBy.patronymic_name created_by_patronymic_name" +
            " ,completedBy.lastname completed_by_lastname, completedBy.firstname completed_by_firstname, completedBy.patronymic_name completed_by_patronymic_name" +
            " ,canceledBy.lastname canceled_by_lastname, canceledBy.firstname canceled_by_firstname, canceledBy.patronymic_name canceled_by_patronymic_name" +
            " from assessment a" +
            " left join employee createdBy on a.created_by=createdBy.id " +
            " left join employee completedBy on a.completed_by=completedBy.id " +
            " left join employee canceledBy on a.canceled_by=canceledBy.id " +
            " where a.employee=:employee" +
            " order by a.created_at desc")
    Flux<AssessmentViewEntry> findByEmployeeId(int employee);
}
