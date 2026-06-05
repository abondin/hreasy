package ru.abondin.hreasy.platform.repo.ba;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface BusinessAccountPositionRepo extends ReactiveCrudRepository<BusinessAccountPositionEntry, Integer> {

    @Query("select * from ba.ba_position where business_account=:baId order by name")
    Flux<BusinessAccountPositionEntry> findAll(int baId);
}
