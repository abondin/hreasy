package ru.abondin.hreasy.platform.repo.employee.admin.kids;

import org.apache.commons.lang3.tuple.Pair;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;
import java.util.Set;

@Repository
public interface EmployeeKidRepo extends ReactiveCrudRepository<EmployeeKidEntry, Integer> {

    String BASE_QUERY = "select k.*, e.display_name parent_display_name" +
            ", (case when e.date_of_dismissal is null or e.date_of_dismissal > :now then true else false end)" +
            " as parent_not_dismissed" +
            " from empl.kids k left join empl.employee e on k.parent=e.id";

    @Query(BASE_QUERY + " order by k.name")
    Flux<EmployeeKidView> findAllKidsWithParentInfo(@Param("now") OffsetDateTime now);

    @Query(BASE_QUERY + " where k.parent=:employeeId and k.id=:employeeKidId")
    Mono<EmployeeKidView> getFullInfo(@Param("employeeId") Integer employeeId, @Param("employeeKidId") Integer employeeKidId);

    @Query(BASE_QUERY + " where (e.email,k.displayName) in (:parentEmailKidDisplayNamePairs)")
    Flux<EmployeeKidView> findByParentEmailAndDisplayNameInLowerCase(@Param("parentEmailKidDisplayNamePairs") Set<Pair<String, String>> parentEmailKidDisplayNamePairs);
}

