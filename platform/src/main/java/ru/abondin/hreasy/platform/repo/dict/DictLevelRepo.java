package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DictLevelRepo extends ReactiveSortingRepository<DictLevelEntry, Integer>, ReactiveCrudRepository<DictLevelEntry, Integer>  {
    
    @Query("select * from dict.level where name=:name")
    Mono<DictLevelEntry> findByName(String name);

    @Query("select * from dict.level l where l.archived = false order by name")
    Flux<DictLevelEntry> findNotArchived();

}
