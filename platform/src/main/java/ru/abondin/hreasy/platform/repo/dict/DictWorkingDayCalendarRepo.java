package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

@Repository
public interface DictWorkingDayCalendarRepo extends ReactiveCrudRepository<DictWorkingDaysCalendarEntry, Integer> {


    @Query("select * from dict.working_days_calendar where year=:year and region=:region and type=:type")
    Mono<DictWorkingDaysCalendarEntry> find(@Param("year") int year, @Param("region") String region, @Param("type") String type);
}
