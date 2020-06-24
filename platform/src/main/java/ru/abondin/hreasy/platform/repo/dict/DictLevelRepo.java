package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DictLevelRepo extends ReactiveCrudRepository<DictLevelEntry, Integer> {
    
    @Query("select * from dict_level where name=:name")
    Mono<DictLevelEntry> findByName(String name);
}
