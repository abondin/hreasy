package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DepartmentRepo extends ReactiveCrudRepository<DepartmentEntry, Integer> {

    @Query("select * from department where name=:name")
    Mono<DepartmentEntry> findByName(String name);
}
