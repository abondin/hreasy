package ru.abondin.hreasy.platform.repo.vacation;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import ru.abondin.hreasy.platform.repo.dict.DictProjectEntry;

import java.time.LocalDate;

@Repository
public interface VacationRepo extends ReactiveCrudRepository<VacationEntry, Integer> {

    @Query("select e.firstname as employee_firstname, e.lastname as employee_lastname, e.patronymic_name as employee_patronymic_name" +
            ", e.current_project as employee_current_project" +
            ", p.name as employee_current_project_name" +
            ", v.* from  vacation v " +
            " inner join employee e on e.id=v.employee" +
            " inner join project p on e.current_project=p.id" +
            " where v.end_date>=:endDateSince" +
            " order by v.end_date asc")
    Flux<VacationView> findAll(LocalDate endDateSince);
}
