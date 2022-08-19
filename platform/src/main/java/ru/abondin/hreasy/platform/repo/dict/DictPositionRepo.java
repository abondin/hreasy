package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DictPositionRepo extends ReactiveSortingRepository<DictPositionEntry, Integer> {
    
    @Query("select * from dict.position where name=:name")
    Mono<DictPositionEntry> findByName(String name);

    @Query("select * from dict.position p where p.archived = false order by name")
    Flux<DictPositionEntry> findNotArchived();

}
