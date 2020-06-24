package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DictPositionRepo extends ReactiveCrudRepository<DictPositionEntry, Integer> {
    
    @Query("select * from dict_position where name=:name")
    Mono<DictPositionEntry> findByName(String name);
}
