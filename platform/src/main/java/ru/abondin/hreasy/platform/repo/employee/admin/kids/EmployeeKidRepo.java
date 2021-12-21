package ru.abondin.hreasy.platform.repo.employee.admin.kids;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

@Repository
public interface EmployeeKidRepo extends ReactiveCrudRepository<EmployeeKidEntry, Integer> {

    @Query("select k.*, e.lastname parent_lastname, e.firstname parent_firstname, e.patronymic_name parent_patronymic_name" +
            ", (case when e.date_of_dismissal is null or e.date_of_dismissal > :now then true else false end)" +
            " as parent_not_dismissed" +
            " from empl.kids k left join empl.employee e on k.parent=e.id")
    Flux<EmployeeKidView> findAllKidsWithParentInfo(@Param("now") OffsetDateTime now);

}

