package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;

import java.util.stream.Stream;

public interface DictOfficeRepo extends ReactiveCrudRepository<DictOfficeEntry, Integer> {
    @Query("select * from dict.office o where o.archived = false order by name asc")
    Flux<DictOfficeEntry> findNotArchived();
}
