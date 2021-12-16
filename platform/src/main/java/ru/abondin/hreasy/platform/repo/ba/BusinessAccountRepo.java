package ru.abondin.hreasy.platform.repo.ba;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BusinessAccountRepo extends ReactiveCrudRepository<BusinessAccountEntry, Integer> {

    String FIND_QUERY_PREFIX = "select ba.*, trim(concat_ws(' ', re.lastname, re.firstname, re.patronymic_name)) as responsible_employee_name " +
            " from ba.business_account ba left join empl.employee re on ba.responsible_employee=re.id";

    @Query(FIND_QUERY_PREFIX +
            "  order by responsible_employee_name")
    Flux<BusinessAccountEntryView> findDetailed();

    @Query(FIND_QUERY_PREFIX +
            "  where ba.archived <> 1 order by responsible_employee_name")
    Flux<BusinessAccountEntryView> findActive();


    @Query(FIND_QUERY_PREFIX + " where ba.id=:baId")
    Mono<BusinessAccountEntryView> findDetailedById(int baId);
}
