package ru.abondin.hreasy.platform.repo.ba;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BusinessAccountRepo extends ReactiveCrudRepository<BusinessAccountEntry, Integer> {

    String FIND_QUERY_PREFIX = "select ba.*" +
            " from ba.business_account ba";

    @Query(FIND_QUERY_PREFIX +
            "  order by name")
    Flux<BusinessAccountEntryView> findDetailed();

    @Query(FIND_QUERY_PREFIX +
            "  where ba.archived != true order by name")
    Flux<BusinessAccountEntryView> findActive();


    @Query(FIND_QUERY_PREFIX + " where ba.id=:baId")
    Mono<BusinessAccountEntryView> findDetailedById(int baId);
}
