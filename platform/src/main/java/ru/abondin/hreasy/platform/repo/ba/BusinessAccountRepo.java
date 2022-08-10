package ru.abondin.hreasy.platform.repo.ba;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface BusinessAccountRepo extends ReactiveCrudRepository<BusinessAccountEntry, Integer> {


    @Query("select * from ba.business_account ba")
    Flux<BusinessAccountEntry> findDetailed();

    @Query("select * from ba.business_account ba where ba.archived != true")
    Flux<BusinessAccountEntry> findActive();


    @Query("select * from ba.business_account ba where ba.id=:baId")
    Mono<BusinessAccountEntry> findDetailedById(int baId);
}
