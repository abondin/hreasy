package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DepartmentRepo extends ReactiveSortingRepository<DepartmentEntry, Integer> {

    @Query("select * from dict.department where name=:name")
    Mono<DepartmentEntry> findByName(String name);

    @Query("select * from dict.department d where d.archived = false order by name")
    Flux<DepartmentEntry> findNotArchived();

}
