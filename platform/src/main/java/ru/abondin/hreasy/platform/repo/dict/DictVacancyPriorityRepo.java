package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DictVacancyPriorityRepo extends ReactiveCrudRepository<DictVacancyPriorityEntry, Integer> {

    @Query("select * from dict_vacancy_priority where name=:name")
    Mono<DictVacancyPriorityEntry> findByName(String name);
}
