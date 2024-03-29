package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DictOrganizationRepo extends ReactiveSortingRepository<DictOrganizationEntry, Integer>, ReactiveCrudRepository<DictOrganizationEntry, Integer> {
    @Query("select * from dict.organization o where o.archived = false order by name")
    Flux<DictOrganizationEntry> findNotArchived();

}
