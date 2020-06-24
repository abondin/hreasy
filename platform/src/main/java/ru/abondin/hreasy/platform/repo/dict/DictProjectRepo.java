package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.time.OffsetDateTime;

@Repository
public interface DictProjectRepo extends ReactiveCrudRepository<DictProjectEntry, Integer> {

    @Query("select * from project p where " +
            "(p.end_date is null or :end is null) or" +
            "p.end_date < :end order by name")
    Flux<DictProjectEntry> findNotEnded(@Param("endDate") OffsetDateTime endDate);
}
