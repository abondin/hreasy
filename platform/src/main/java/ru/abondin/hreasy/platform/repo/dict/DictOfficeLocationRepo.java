package ru.abondin.hreasy.platform.repo.dict;

import org.reactivestreams.Publisher;
import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.data.repository.reactive.ReactiveSortingRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

@Repository
public interface DictOfficeLocationRepo extends ReactiveSortingRepository<DictOfficeLocationEntry, Integer>, ReactiveCrudRepository<DictOfficeLocationEntry, Integer> {
    @Query("select * from dict.office_location o where o.archived = false order by name")
    Flux<DictOfficeLocationEntry> findNotArchived();

    @Query("select * from dict.office_location o where o.archived = false and id=:id")
    Mono<DictOfficeLocationEntry> findNotArchived(@Param("id") int id);

    @Query("select l.*, o.name office_name from dict.office_location l left join dict.office o on l.office_id = o.id")
    Flux<DictOfficeLocationView> findAllView();
}
