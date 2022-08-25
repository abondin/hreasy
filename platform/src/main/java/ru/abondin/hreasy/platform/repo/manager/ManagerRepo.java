package ru.abondin.hreasy.platform.repo.manager;

import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

@Repository
public interface ManagerRepo extends ReactiveCrudRepository<ManagerEntry, Integer> {

    final String defaultSelectQuery = """
            select m.*,
            trim(concat_ws(' ', e.lastname, e.firstname, e.patronymic_name)) as employee_display_name
            , (e.date_of_dismissal is null or e.date_of_dismissal > :now) as employee_active
            ,CASE WHEN m.object_type='project' THEN p.name
                  WHEN m.object_type='business_account' THEN ba.name
                  WHEN m.object_type='department' THEN d.name
                  ELSE concat('UNSUPPORTED OBJECT TYPE', m.object_type)
             END as object_name
            from empl.manager m
            left join empl.employee e on m.employee=e.id
            left join ba.business_account ba on m.object_id=ba.id\s
            left join proj.project p on m.object_id=p.id\s
            left join dict.department d on m.object_id=d.id\s
            """;

    @Query(defaultSelectQuery+" order by employee_display_name asc")
    Flux<ManagerView> findDetailed(@Param("now") OffsetDateTime now);

    @Query(defaultSelectQuery+" where object_type=:objectType and object_id=:objectId order by employee_display_name asc")
    Flux<ManagerView> findByObjectDetailed(OffsetDateTime now, String objectType, int objectId);
}

