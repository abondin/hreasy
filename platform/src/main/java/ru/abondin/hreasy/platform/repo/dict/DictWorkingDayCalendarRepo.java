package ru.abondin.hreasy.platform.repo.dict;

import org.springframework.data.r2dbc.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

@Repository
public interface DictWorkingDayCalendarRepo extends ReactiveCrudRepository<DictWorkingDaysCalendarEntry, Integer> {


    @Query("select * from dict.working_days_calendar where year in (:year) and region=:region and type=:type")
    Flux<DictWorkingDaysCalendarEntry> find(@Param("years") List<Integer> years, @Param("region") String region, @Param("type") String type);
}
