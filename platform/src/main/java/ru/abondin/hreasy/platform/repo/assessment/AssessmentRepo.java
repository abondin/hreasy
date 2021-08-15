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
            ", e.date_of_employment employee_date_of_employment,  a.* " +
            " from employee e left join assessment a on e.id=a.employee" +
            " where e.date_of_dismissal is null or e.date_of_dismissal > :now" +
            " and a.canceled_at is null or a.canceled_at > :now" +
            " order by a.created_at desc, e.date_of_employment desc")
    Flux<EmployeeAssessmentEntry> findNotCanceledAssessmentForNotFired(@Param("now") OffsetDateTime now);

}
