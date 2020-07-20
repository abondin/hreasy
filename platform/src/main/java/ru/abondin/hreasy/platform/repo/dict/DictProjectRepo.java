package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DictProjectRepo extends ReactiveCrudRepository<DictProjectEntry, Integer> {

    @Query("select * from project p order by name")
    Flux<DictProjectEntry> findAll();
}
