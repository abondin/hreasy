package ru.abondin.hreasy.platform.repo.ba;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface BusinessAccountPositionRepo extends ReactiveCrudRepository<BusinessAccountPositionEntry, Integer> {

    String FIND_ALL_QUERY_PREFIX = "select * from ba_position where business_account=:baId";

    @Query(FIND_ALL_QUERY_PREFIX + " order by name")
    Flux<BusinessAccountPositionEntry> findAll(int baId);

    @Query(FIND_ALL_QUERY_PREFIX + " and archived_at is null order by name")
    Flux<BusinessAccountPositionEntry> findNotArchived(int baId);

}
