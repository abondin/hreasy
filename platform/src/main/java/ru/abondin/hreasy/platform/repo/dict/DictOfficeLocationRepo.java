package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

@Repository
public interface DictOfficeLocationRepo extends ReactiveSortingRepository<DictOfficeLocationEntry, Integer>, ReactiveCrudRepository<DictOfficeLocationEntry, Integer>  {
    @Query("select * from dict.office_location o where o.archived = false order by name")
    Flux<DictOfficeLocationEntry> findNotArchived();

}
