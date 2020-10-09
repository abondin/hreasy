package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
public interface DictProjectRepo extends ReactiveCrudRepository<DictProjectEntry, Integer> {

    @Query("select * from project p order by name")
    Flux<DictProjectEntry> findAll();

    @Query("select * from project where id in (select current_project from employee where id=:employeeId)")
    Mono<DictProjectEntry> getEmployeeCurrentProject(int employeeId);

    @Query("select * from project p where p.id in (:ids) order by name")
    Flux<DictProjectEntry> findByIds(List<Integer> ids);


}
